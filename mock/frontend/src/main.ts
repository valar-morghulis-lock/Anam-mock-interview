import { createClient, AnamEvent, type AnamClient } from "@anam-ai/js-sdk";
import "./style.css";

const API_BASE = "http://localhost:8080/api/interviews";

type QuestionSummary = { sessionQuestionId: string; text: string; sequenceNo: number };
type SessionResponse = {
  sessionId: string;
  role: string;
  seniority: string;
  personaStyle: string;
  questions: QuestionSummary[];
};
type AnswerSummary = {
  questionText: string;
  hasSituation: boolean;
  hasTask: boolean;
  hasAction: boolean;
  hasResult: boolean;
  score: number;
  improvement: string;
};
type FeedbackReportResponse = {
  reportId: string;
  sessionId: string;
  overallStrengths: string;
  createdAt: string;
  answers: AnswerSummary[];
};

// ---- DOM refs ----
const setupForm = document.getElementById("setup-form") as HTMLFormElement;
const sessionPanel = document.getElementById("session-panel")!;
const reportPanel = document.getElementById("report-panel")!;
const sessionMeta = document.getElementById("session-meta")!;
const qIndexEl = document.getElementById("q-index")!;
const qTotalEl = document.getElementById("q-total")!;
const qTextEl = document.getElementById("q-text")!;
const signalBar = document.getElementById("signal-bar")!;
const beginBtn = document.getElementById("begin-btn") as HTMLButtonElement;
const nextBtn = document.getElementById("next-btn") as HTMLButtonElement;
const skipBtn = document.getElementById("skip-btn") as HTMLButtonElement;
const endBtn = document.getElementById("end-btn") as HTMLButtonElement;
const statusPill = document.getElementById("status-pill")!;
const statusText = document.getElementById("status-text")!;
const stageOverlay = document.getElementById("stage-overlay")!;
const stageOverlayText = document.getElementById("stage-overlay-text")!;
const stageConnecting = document.getElementById("stage-connecting")!;
const transcriptLog = document.getElementById("transcript-log")!;
const jumpLatestBtn = document.getElementById("jump-latest-btn") as HTMLButtonElement;
const toastStack = document.getElementById("toast-stack")!;
const competencyError = document.getElementById("competency-error")!;
const technicalCheckbox = document.getElementById("technical-checkbox") as HTMLInputElement;
const languageField = document.getElementById("language-field")!;
const languageSelect = document.getElementById("language") as HTMLSelectElement;

technicalCheckbox.addEventListener("change", () => {
  languageField.classList.toggle("hidden", !technicalCheckbox.checked);
});

const reportMeta = document.getElementById("report-meta")!;
const scoreRingRow = document.getElementById("score-ring-row")!;
const reportSummary = document.getElementById("report-summary")!;
const answerListEl = document.getElementById("answer-list")!;
const downloadPdfBtn = document.getElementById("download-pdf-btn") as HTMLAnchorElement;
const restartBtn = document.getElementById("restart-btn") as HTMLButtonElement;

// ---- State ----
let session: SessionResponse | null = null;
let currentIndex = 0;
let anamClient: AnamClient | null = null;
let lastMessageCount = 0;
let autoScroll = true;

// ---- Toasts ----
function showToast(message: string, type: "error" | "success" = "error", timeoutMs = 6000) {
  const toast = document.createElement("div");
  toast.className = "toast" + (type === "success" ? " toast-success" : "");
  toast.innerHTML = `<span>${message}</span><button class="toast-close" aria-label="Dismiss">×</button>`;
  toastStack.appendChild(toast);

  const remove = () => toast.remove();
  toast.querySelector(".toast-close")!.addEventListener("click", remove);
  if (timeoutMs > 0) setTimeout(remove, timeoutMs);
}

// ---- Button loading helper ----
function setButtonLoading(btn: HTMLButtonElement, loading: boolean) {
  const label = btn.querySelector(".btn-label");
  const spinner = btn.querySelector(".spinner");
  btn.disabled = loading;
  if (spinner) spinner.classList.toggle("hidden", !loading);
  if (label && loading) label.setAttribute("data-prev", label.textContent ?? "");
}

function setStatus(state: "idle" | "connecting" | "live" | "ended", label: string) {
  statusPill.setAttribute("data-state", state);
  statusText.textContent = label;
}

function switchView(view: "setup" | "session" | "report") {
  setupForm.classList.toggle("hidden", view !== "setup");
  sessionPanel.classList.toggle("hidden", view !== "session");
  reportPanel.classList.toggle("hidden", view !== "report");
}

function renderSignalBar() {
  if (!session) return;
  signalBar.innerHTML = "";
  session.questions.forEach((_, i) => {
    const seg = document.createElement("div");
    seg.className = "seg" + (i < currentIndex ? " done" : i === currentIndex ? " current" : "");
    signalBar.appendChild(seg);
  });
}

function renderCurrentQuestion() {
  if (!session) return;
  const q = session.questions[currentIndex];
  qIndexEl.textContent = String(Math.min(currentIndex + 1, session.questions.length));
  qTotalEl.textContent = String(session.questions.length);
  qTextEl.textContent = q ? q.text : "All questions answered.";
  renderSignalBar();

  const atEnd = currentIndex >= session.questions.length;
  nextBtn.disabled = atEnd;
  skipBtn.disabled = atEnd;
}

function appendTranscriptLine(speaker: "INTERVIEWER" | "CANDIDATE", content: string) {
  const empty = transcriptLog.querySelector(".transcript-empty");
  if (empty) empty.remove();

  const div = document.createElement("div");
  div.className = "msg " + (speaker === "CANDIDATE" ? "msg-candidate" : "msg-interviewer");
  const time = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  const who = document.createElement("div");
  who.className = "who";
  who.innerHTML = `<span>${speaker === "CANDIDATE" ? "You" : "Gabriel"}</span><span>${time}</span>`;
  const body = document.createElement("p");
  body.style.margin = "0";
  body.textContent = content;
  div.appendChild(who);
  div.appendChild(body);
  transcriptLog.appendChild(div);

  if (autoScroll) {
    transcriptLog.scrollTop = transcriptLog.scrollHeight;
    jumpLatestBtn.classList.add("hidden");
  } else {
    jumpLatestBtn.classList.remove("hidden");
  }
}

transcriptLog.addEventListener("scroll", () => {
  const atBottom = transcriptLog.scrollHeight - transcriptLog.scrollTop - transcriptLog.clientHeight < 20;
  autoScroll = atBottom;
  if (atBottom) jumpLatestBtn.classList.add("hidden");
});

jumpLatestBtn.addEventListener("click", () => {
  transcriptLog.scrollTop = transcriptLog.scrollHeight;
  autoScroll = true;
  jumpLatestBtn.classList.add("hidden");
});

async function postTranscriptMessage(speaker: "INTERVIEWER" | "CANDIDATE", content: string) {
  if (!session) return;
  const sessionQuestionId = session.questions[currentIndex]?.sessionQuestionId ?? null;
  try {
    await fetch(`${API_BASE}/${session.sessionId}/transcript`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sessionQuestionId, speaker, content }),
    });
  } catch (err) {
    console.error("Failed to persist transcript message", err);
    showToast("Couldn't save that message — check the backend connection.");
  }
}

// ---- Setup form ----
setupForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const submitBtn = document.getElementById("create-session-btn") as HTMLButtonElement;

  const role = (document.getElementById("role") as HTMLInputElement).value;
  const seniority = (document.getElementById("seniority") as HTMLSelectElement).value;
  const personaStyle = (document.getElementById("personaStyle") as HTMLSelectElement).value;
  const questionsPerCompetency = Number(
    (document.getElementById("questionsPerCompetency") as HTMLInputElement).value
  );
  const timeLimitSec = Number((document.getElementById("timeLimitSec") as HTMLInputElement).value);
  const competencyNames = Array.from(
    document.querySelectorAll<HTMLInputElement>("#competency-list input:checked")
  ).map((el) => el.value);

  if (competencyNames.length === 0) {
    competencyError.classList.remove("hidden");
    return;
  }
  competencyError.classList.add("hidden");

  const language = technicalCheckbox.checked ? languageSelect.value : null;

  setButtonLoading(submitBtn, true);

  try {
    const res = await fetch(API_BASE, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        role, seniority, personaStyle, competencyNames, language, questionsPerCompetency, timeLimitSec,
      }),
    });

    if (!res.ok) throw new Error(`Setup failed: ${res.status}`);
    session = await res.json();
    currentIndex = 0;

    switchView("session");
    sessionMeta.textContent = `${session!.role} · ${session!.seniority} · ${session!.questions.length} questions`;
    renderCurrentQuestion();
  } catch (err) {
    console.error(err);
    showToast("Could not create session. Check the backend is running on localhost:8080.");
  } finally {
    setButtonLoading(submitBtn, false);
  }
});

// ---- Begin interview ----
beginBtn.addEventListener("click", async () => {
  if (!session) return;
  setButtonLoading(beginBtn, true);
  setStatus("connecting", "Connecting…");
  stageConnecting.classList.remove("hidden");

  try {
    const tokenRes = await fetch(`${API_BASE}/${session.sessionId}/session-token`, { method: "POST" });
    if (!tokenRes.ok) throw new Error(`Token request failed: ${tokenRes.status}`);
    const { sessionToken } = await tokenRes.json();

    await fetch(`${API_BASE}/${session.sessionId}/start`, { method: "POST" });

    anamClient = createClient(sessionToken);

    anamClient.addListener(AnamEvent.CONNECTION_ESTABLISHED, () => {
      console.log("Anam connection established");
    });

    anamClient.addListener(AnamEvent.SESSION_READY, () => {
      setStatus("live", "Live");
      stageOverlay.classList.add("hidden");
      stageConnecting.classList.add("hidden");
      beginBtn.classList.add("hidden");
      nextBtn.classList.remove("hidden");
      skipBtn.classList.remove("hidden");
      endBtn.classList.remove("hidden");
    });

    anamClient.addListener(AnamEvent.MESSAGE_HISTORY_UPDATED, (messages: any[]) => {
      const newMessages = messages.slice(lastMessageCount);
      lastMessageCount = messages.length;
      newMessages.forEach((m) => {
        const speaker: "INTERVIEWER" | "CANDIDATE" = m.role === "user" ? "CANDIDATE" : "INTERVIEWER";
        appendTranscriptLine(speaker, m.content);
        postTranscriptMessage(speaker, m.content);
      });
    });

    anamClient.addListener(AnamEvent.CONNECTION_CLOSED, () => {
      setStatus("ended", "Disconnected");
    });

    await anamClient.streamToVideoElement("persona-video");
  } catch (err) {
    console.error(err);
    showToast("Could not start the interview. Check the backend and Anam connection.");
    setStatus("idle", "Not started");
    stageConnecting.classList.add("hidden");
    stageOverlayText.textContent = "Gabriel is waiting to begin";
    stageOverlay.classList.remove("hidden");
    setButtonLoading(beginBtn, false);
  }
});

// ---- Next / skip ----
nextBtn.addEventListener("click", () => {
  if (!session) return;
  currentIndex = Math.min(currentIndex + 1, session.questions.length);
  renderCurrentQuestion();
});

skipBtn.addEventListener("click", async () => {
  if (!session) return;
  const q = session.questions[currentIndex];
  if (q) {
    try {
      await fetch(`${API_BASE}/${session.sessionId}/transcript/questions/${q.sessionQuestionId}/skip`, {
        method: "POST",
      });
    } catch (err) {
      console.error("Failed to mark question skipped", err);
    }
  }
  currentIndex = Math.min(currentIndex + 1, session.questions.length);
  renderCurrentQuestion();
});

// ---- End interview → generate + show report ----
function scoreColor(score: number): string {
  if (score >= 4) return "var(--accent)";
  if (score >= 3) return "var(--warn)";
  return "var(--danger)";
}

function renderScoreRing(score: number): HTMLElement {
  const pct = (score / 5) * 100;
  const color = scoreColor(score);
  const ring = document.createElement("div");
  ring.className = "score-ring";
  ring.style.background = `conic-gradient(${color} ${pct}%, var(--border) ${pct}% 100%)`;
  ring.innerHTML = `<span style="background:var(--bg); width:42px; height:42px; border-radius:50%; display:flex; align-items:center; justify-content:center;">${score}</span>`;
  return ring;
}

function renderReport(report: FeedbackReportResponse) {
  reportMeta.textContent = `${session?.role ?? ""} · ${session?.seniority ?? ""} · generated ${new Date(report.createdAt).toLocaleString()}`;

  scoreRingRow.innerHTML = "";
  report.answers.forEach((a) => scoreRingRow.appendChild(renderScoreRing(a.score)));

  reportSummary.textContent = report.overallStrengths;

  answerListEl.innerHTML = "";
  report.answers.forEach((a, i) => {
    const card = document.createElement("div");
    card.className = "answer-card";

    const head = document.createElement("div");
    head.className = "answer-card-head";
    head.innerHTML = `
      <p class="answer-card-question">Q${i + 1}. ${a.questionText}</p>
      <span class="answer-score-badge" style="color:${scoreColor(a.score)}; border:1px solid ${scoreColor(a.score)};">${a.score} / 5</span>
    `;
    card.appendChild(head);

    const tags = document.createElement("div");
    tags.className = "star-tags";
    const starMap: [string, boolean][] = [
      ["Situation", a.hasSituation], ["Task", a.hasTask], ["Action", a.hasAction], ["Result", a.hasResult],
    ];
    starMap.forEach(([label, present]) => {
      const tag = document.createElement("span");
      tag.className = "star-tag" + (present ? " present" : "");
      tag.textContent = label;
      tags.appendChild(tag);
    });
    card.appendChild(tags);

    const improvement = document.createElement("p");
    improvement.className = "answer-improvement";
    improvement.textContent = a.improvement;
    card.appendChild(improvement);

    answerListEl.appendChild(card);
  });

  downloadPdfBtn.href = `${API_BASE}/${report.sessionId}/report/pdf`;
  switchView("report");
}

endBtn.addEventListener("click", async () => {
  if (!session) return;
  setButtonLoading(endBtn, true);

  try {
    anamClient?.stopStreaming();
    await fetch(`${API_BASE}/${session.sessionId}/end?completedNaturally=true`, { method: "POST" });
    setStatus("ended", "Generating report…");

    const reportRes = await fetch(`${API_BASE}/${session.sessionId}/report`, { method: "POST" });
    if (!reportRes.ok) throw new Error(`Report generation failed: ${reportRes.status}`);
    const report: FeedbackReportResponse = await reportRes.json();

    setStatus("ended", "Ended");
    showToast("Feedback report ready.", "success");
    renderReport(report);
  } catch (err) {
    console.error(err);
    showToast("Interview ended, but the report couldn't be generated. Check your LLM API key.");
    setStatus("ended", "Ended");
  } finally {
    setButtonLoading(endBtn, false);
  }
});

// ---- Restart ----
restartBtn.addEventListener("click", () => {
  session = null;
  currentIndex = 0;
  lastMessageCount = 0;
  anamClient = null;

  setupForm.reset();
  competencyError.classList.add("hidden");
  languageField.classList.add("hidden");
  transcriptLog.innerHTML = '<p class="transcript-empty">Nothing said yet.</p>';
  beginBtn.classList.remove("hidden");
  nextBtn.classList.add("hidden");
  skipBtn.classList.add("hidden");
  endBtn.classList.add("hidden");
  stageOverlayText.textContent = "Gabriel is waiting to begin";
  stageOverlay.classList.remove("hidden");

  setStatus("idle", "Not started");
  switchView("setup");
});
