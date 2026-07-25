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

// ---- DOM refs ----
const setupForm = document.getElementById("setup-form") as HTMLFormElement;
const sessionPanel = document.getElementById("session-panel")!;
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
const transcriptLog = document.getElementById("transcript-log")!;

// ---- State ----
let session: SessionResponse | null = null;
let currentIndex = 0;
let anamClient: AnamClient | null = null;
let lastMessageCount = 0;

function setStatus(state: "idle" | "connecting" | "live" | "ended", label: string) {
  statusPill.setAttribute("data-state", state);
  statusText.textContent = label;
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
  qIndexEl.textContent = String(currentIndex + 1);
  qTotalEl.textContent = String(session.questions.length);
  qTextEl.textContent = q ? q.text : "Interview complete.";
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
  const who = document.createElement("span");
  who.className = "who";
  who.textContent = speaker === "CANDIDATE" ? "You" : "Gabriel";
  const body = document.createElement("p");
  body.style.margin = "0";
  body.textContent = content;
  div.appendChild(who);
  div.appendChild(body);
  transcriptLog.appendChild(div);
  transcriptLog.scrollTop = transcriptLog.scrollHeight;
}

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
  }
}

// ---- Setup form ----
setupForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const submitBtn = document.getElementById("create-session-btn") as HTMLButtonElement;
  submitBtn.disabled = true;
  submitBtn.textContent = "Creating…";

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
    alert("Select at least one competency.");
    submitBtn.disabled = false;
    submitBtn.textContent = "Create session";
    return;
  }

  try {
    const res = await fetch(API_BASE, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        role,
        seniority,
        personaStyle,
        competencyNames,
        questionsPerCompetency,
        timeLimitSec,
      }),
    });

    if (!res.ok) throw new Error(`Setup failed: ${res.status}`);
    session = await res.json();
    currentIndex = 0;

    setupForm.classList.add("hidden");
    sessionPanel.classList.remove("hidden");
    sessionMeta.textContent = `${session!.role} · ${session!.seniority} · ${session!.questions.length} questions`;
    renderCurrentQuestion();
  } catch (err) {
    console.error(err);
    alert("Could not create session. Check the backend is running on localhost:8080.");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Create session";
  }
});

// ---- Begin interview ----
beginBtn.addEventListener("click", async () => {
  if (!session) return;
  beginBtn.disabled = true;
  setStatus("connecting", "Connecting…");

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
    alert("Could not start the interview. Check the backend and Anam connection.");
    setStatus("idle", "Not started");
    beginBtn.disabled = false;
  }
});

// ---- Next / skip / end ----
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

endBtn.addEventListener("click", async () => {
  if (!session) return;
  endBtn.disabled = true;
  try {
    anamClient?.stopStreaming();
    await fetch(`${API_BASE}/${session.sessionId}/end?completedNaturally=true`, { method: "POST" });
    setStatus("ended", "Ended");
    stageOverlay.classList.remove("hidden");
    stageOverlay.querySelector("span")!.textContent = "Interview ended";
    nextBtn.classList.add("hidden");
    skipBtn.classList.add("hidden");
    endBtn.classList.add("hidden");
  } catch (err) {
    console.error(err);
  }
});
