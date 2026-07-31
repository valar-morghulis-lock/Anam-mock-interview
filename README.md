# Mock Interview Coach

A behavioural mock-interview platform with a live, photorealistic AI interviewer. Practice with a real avatar that asks questions, probes vague answers, and scores your responses against the STAR framework when you're done.

Built as an implementation of [Coding Challenge #128: Mock Interview](https://codingchallenges.substack.com/p/coding-challenge-128-mock-interview).

> **Gabriel** conducts the interview live over video and voice, powered by [Anam](https://anam.ai). When you're done, **Groq** scores every answer against Situation / Task / Action / Result and writes a feedback report.

---

## What it does

1. **Set up an interview** — pick a role, seniority level, interviewer style, and the competencies you want to be tested on: leadership, conflict, failure, teamwork, delivery, and now **technical** (with a programming language of your choice).
2. **Meet your interviewer** — a live, photorealistic avatar (Gabriel) asks your questions one at a time over real-time video and voice, and follows up on vague answers instead of moving on.
3. **Answer out loud** — your responses are transcribed and stored automatically as the conversation happens.
4. **Get scored** — after the interview, every answer is analyzed for STAR structure and given a 1–5 score with a specific tip for improvement, plus an overall summary. Unanswered questions are marked explicitly rather than scored as if a blank answer were a bad one.

---

## Architecture

```
┌──────────────┐        setup / lifecycle        ┌───────────────────┐
│   Frontend   │ ───────────────────────────────▶│   Spring Boot 4   │
│  (Vite + TS) │◀─────────────────────────────────      Backend       │
└──────┬───────┘        session token             └─────────┬─────────┘
       │                                                     │
       │ live video/audio (WebRTC)                           │ REST
       ▼                                                     ▼
┌──────────────┐                                   ┌───────────────────┐
│   Anam API   │                                   │    PostgreSQL     │
│ (Gabriel /   │                                   │  (Flyway-managed) │
│  GPT OSS 120B)│                                  └───────────────────┘
└──────────────┘
                                                     ┌───────────────────┐
                                                     │     Groq API      │
                                                     │ (STAR scoring +   │
                                                     │ question generation)│
                                                     └───────────────────┘
```

The backend never exposes API keys to the browser. It exchanges its own Anam and Groq credentials server-side for short-lived session tokens and scored feedback, keeping both providers' keys off the client entirely.

---

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4.1 |
| Persistence | PostgreSQL 17, Flyway migrations, Spring Data JPA / Hibernate, `pg_trgm` for fuzzy dedup |
| Live interviewer | [Anam](https://anam.ai) — real-time avatar, voice, and LLM streaming |
| Feedback scoring & question generation | [Groq](https://groq.com) (default, free tier) — swappable via the `LlmClient` interface; Anthropic Claude supported as an opt-in alternative |
| PDF export | [OpenPDF](https://github.com/LibrePDF/OpenPDF) |
| Frontend | Vite, TypeScript, `@anam-ai/js-sdk` |
| Local infra | Docker Compose (Postgres + pgAdmin) |

---

## Getting started

### Prerequisites

- Java 21
- Docker Desktop
- Node.js 18+
- An [Anam](https://anam.ai) API key (free tier: 30 min/month, 3 min/session)
- A [Groq](https://console.groq.com) API key (free, no card required)

### 1. Start the database

```bash
docker compose up -d
```

Spins up Postgres and pgAdmin. Flyway applies all migrations automatically on the backend's first run.

### 2. Configure credentials

The backend needs an Anam key (live interviewer) and an LLM key for feedback scoring and technical question generation, read via `application.yml`:

```yaml
anam:
  api-key: ${ANAM_API_KEY:not-set}
groq:
  api-key: ${GROQ_API_KEY:not-set}
llm:
  api-key: ${LLM_API_KEY:not-set}
```

**Groq is the default provider** for both feedback scoring and dynamic technical question generation — free, no payment method required. Anthropic is supported as an opt-in alternative via the `anthropic-llm` Spring profile, if you'd rather use Claude for scoring.

All keys fall back to `not-set` if unset, so the app **starts** without them — but the live interview (`/session-token`) and feedback scoring (`/report`) endpoints will fail until real keys are provided.

**Getting the keys:**
- **Anam**: sign up free at [anam.ai](https://anam.ai) → dashboard → API keys. Free tier includes 30 minutes of conversation per month, 3 minutes per session.
- **Groq** (recommended, free): sign up at [console.groq.com](https://console.groq.com) → API Keys → Create API Key. No card required, key starts with `gsk_`.
- **Anthropic** (optional): [console.anthropic.com](https://console.anthropic.com) → API Keys → Create Key. Requires a payment method; billed per token. Only needed if you activate the `anthropic-llm` profile instead of using Groq.

**Setting the env vars:**

```powershell
# PowerShell — current session only
$env:ANAM_API_KEY = "your-anam-key"
$env:GROQ_API_KEY = "your-groq-key"

# Persistent across sessions
setx ANAM_API_KEY "your-anam-key"
setx GROQ_API_KEY "your-groq-key"
```

> ⚠️ **If you run the app from an IDE** (IntelliJ, VS Code) rather than a terminal, the IDE's run configuration has its **own separate environment** — setting the var in your shell won't reach it. Add both variables directly in the run configuration's environment-variables field instead (in IntelliJ: *Run → Edit Configurations → Environment variables*), and use `;` to separate multiple entries on one line, e.g. `ANAM_API_KEY=xxx;GROQ_API_KEY=xxx` — not a comma.

**Swapping the interviewer's avatar/voice/LLM:** these are hardcoded constants in `PersonaBuilderService` (`AVATAR_ID`, `VOICE_ID`, `LLM_ID`). Browse available options and copy IDs from Anam's API directly:

```bash
curl -H "Authorization: Bearer $ANAM_API_KEY" https://api.anam.ai/v1/avatars
curl -H "Authorization: Bearer $ANAM_API_KEY" https://api.anam.ai/v1/voices
curl -H "Authorization: Bearer $ANAM_API_KEY" https://api.anam.ai/v1/llms
```

### 3. Run the backend

```bash
./mvnw spring-boot:run
```

Starts on `http://localhost:8080`.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5173` (pinned via `vite.config.ts` with `strictPort: true` — if that port is already held by a stale process, the dev server will fail loudly rather than silently moving to another port). Set up an interview, click **Begin interview**, and Gabriel will appear live.

### Quick end-to-end test (no live interview needed)

`scripts/test-report-flow.ps1` exercises the full scoring pipeline without needing a live Anam session — it creates a session, seeds a realistic fake answer for every question, generates the STAR feedback report via Groq, and downloads it as a PDF:

```powershell
.\scripts\test-report-flow.ps1
```

Useful for verifying the backend, Groq integration, and PDF export all work correctly before testing the full live-interview flow through the UI. Accepts parameters to customize the role, seniority, persona style, and competencies — see the script's header comment for details.

---

## API reference

| Endpoint | Description |
|---|---|
| `POST /api/interviews` | Create a session — selects random questions per chosen competency; generates a fresh batch of technical questions on first use for a given role/language if the pool is thin |
| `POST /api/interviews/{id}/session-token` | Exchange for a live Anam session token, with a dynamically generated persona script based on the session's actual questions |
| `POST /api/interviews/{id}/start` | Mark the session as in progress |
| `POST /api/interviews/{id}/transcript` | Append a transcript message (interviewer or candidate) |
| `POST /api/interviews/{id}/transcript/questions/{qid}/skip` | Mark a question as skipped |
| `GET /api/interviews/{id}/transcript` | Retrieve the full transcript |
| `POST /api/interviews/{id}/end` | Mark the session as completed or abandoned |
| `POST /api/interviews/{id}/report` | Generate the STAR-scored feedback report |
| `GET /api/interviews/{id}/report/pdf` | Download the feedback report as a PDF (requires a report to already exist) |

All errors are returned as [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) `ProblemDetail` responses.

**Valid values for `POST /api/interviews`:**
- `seniority`: `JUNIOR`, `MID`, `SENIOR`, `STAFF`
- `personaStyle`: `SUPPORTIVE`, `BAR_RAISER`
- `competencyNames`: `leadership`, `conflict`, `failure`, `teamwork`, `delivery`, `technical`
- `language`: optional, only used when `technical` is included (e.g. `Java`, `Python`, `JavaScript`, `Go`, `C++`)

**CORS:** the backend only allows requests from `http://localhost:5173` by default (`CorsConfig`). If you run the frontend on a different port or host, update the `allowedOrigins` value there.

---

## Project structure

```
mock/
├── src/main/java/anam/interview/mock/
│   ├── controllers/     REST endpoints
│   ├── service/         business logic (setup, lifecycle, transcript, feedback, persona building, question generation)
│   ├── entities/        JPA entities
│   ├── repositories/    Spring Data repositories
│   ├── anam/            Anam client + persona/session-token DTOs
│   ├── llm/             Groq/Anthropic clients, prompt builder, response parsing
│   ├── util/            shared helpers (tag normalization)
│   └── exception/       custom exceptions + global RFC 7807 handler
├── src/main/resources/db/migration/    Flyway SQL migrations
├── docker-compose.yml                  Postgres + pgAdmin
├── scripts/                            dev/test tooling (end-to-end report flow script)
└── frontend/                           Vite + TypeScript interview console UI
```

---

## How the live interview actually works

The interviewer isn't scripted client-side — each session's specific questions (drawn randomly from the chosen competencies) are compiled into a **dynamic system prompt** and sent to Anam per session. Gabriel, running on GPT OSS 120B, follows that exact question list live, asking one at a time and waiting for a complete answer before moving on. Two interviewer styles are supported — a supportive coach and a formal bar-raiser — each with a distinct tone baked into the same generated script.

Once the interview ends, the full transcript is analyzed by Groq, answer by answer, for STAR structure (Situation, Task, Action, Result), producing a 1–5 score and a specific improvement tip per answer, plus an overall summary of the candidate's strengths.

### How technical questions are generated

Selecting the **Technical** competency and a programming language doesn't pull from a fixed bank the way the behavioural competencies do. Instead:

1. The backend checks whether at least 5 questions already exist for that exact (role, language) pair.
2. If not, it asks Groq to generate a fresh batch, tagged with the normalized role and language.
3. Each generated question is checked against existing ones using Postgres `pg_trgm` similarity before being inserted, to avoid near-duplicate phrasing.
4. Future sessions for the same role/language reuse this pool for free — generation only happens once per unique (role, language) combination, not per session.

Role and language values are normalized (trimmed, lowercased) before matching, so "Java Backend Engineer" and "java backend engineer" share the same pool instead of each starting a new one.

---

## Known limitations

- Question progression in the UI is currently a manual "Next question" click rather than an automatic signal from the conversation itself.
- Interviewer avatar/voice/LLM IDs are hardcoded to one persona pairing (Gabriel + a matching male voice); swapping personas means editing a constant, not yet a full persona catalog.
- **Anam's free tier caps live sessions at 3 minutes.** With follow-up probing on vague answers, this comfortably fits only 1–2 questions in practice — a realistic multi-question interview (the app's default is 4) requires a paid Anam plan.
- Technical question dedup uses trigram text similarity only, not embeddings — may miss semantically similar but differently-worded duplicates.
- No content moderation pass on generated technical questions before they're inserted into the shared pool.
- No authentication — this is a local practice tool, not a multi-user deployment.

## Going further

Ideas to take the coach beyond its current scope, and where the project stands against each one today:

- **Per-answer timer** — not implemented. `time_limit_sec` currently caps the whole session in Anam, but there's no per-question timer surfaced in the UI to help candidates practise keeping individual answers to two or three minutes.
- **Printable / PDF feedback report** — ✅ implemented. `GET /report/pdf` renders the overall summary and full per-answer STAR breakdown (via OpenPDF) directly from the persisted report data, no re-scoring required.
- **Technical interview mode** — ✅ implemented. A dedicated `technical` competency generates role- and language-specific questions on demand via Groq, deduped and reused across sessions. **System design** specifically is not yet a distinct mode — technical questions are general-purpose per language, not scoped to architecture/design-specific prompts, and still scored against the same STAR rubric as behavioural questions rather than a technical-appropriate rubric.
- **Bring your own LLM to the live conversation** — not implemented. The live interviewer currently runs on one of Anam's built-in models (GPT OSS 120B); Groq (or optionally Anthropic) is only used after the fact to score the transcript and to generate technical questions ahead of time. Wiring an LLM into the live conversation itself would mean switching to Anam's `CUSTOMER_CLIENT_V1` custom-LLM mode, handling `MESSAGE_HISTORY_UPDATED` events, and streaming responses back via Anam's talk-message API — meaningfully more work than the current setup, but it would let the same model that scores answers also decide what to ask next.

---

## Credits

- Challenge design: [Coding Challenges — Mock Interview (#128)](https://codingchallenges.substack.com/p/coding-challenge-128-mock-interview)
- Live avatar, voice, and LLM streaming: [Anam](https://anam.ai)
- Feedback scoring and technical question generation: [Groq](https://groq.com)