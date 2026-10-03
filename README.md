# CareerPilot: AI-Assisted Placement & Job Management Platform

Students check eligibility, see a skill-match score for every job, apply, and track each application through a hiring pipeline. Recruiters manage companies, jobs and candidates. AI helps with resume feedback, JD parsing and interview practice, but **core matching and security never depend on AI**.

**Stack:** Java 17 · Spring Boot 3 · Spring Security + JWT · Spring Data JPA/Hibernate · H2 (zero-setup) or PostgreSQL · React 18 + Vite · Recharts · JUnit 5 + Mockito · Docker Compose

## Problem statement
Students apply without knowing whether they meet eligibility rules, which skills they lack, or how to track many applications. CareerPilot puts all of this in one place.

## Quick start in VS Code (about 3 minutes)
Prerequisites: **JDK 17+**, **Maven 3.9+**, **Node 18+**. Install the recommended extensions when VS Code prompts (Extension Pack for Java, Spring Boot Extension Pack).

```bash
# 1. Backend  (terminal 1)  ->  http://localhost:8080
cd backend
mvn spring-boot:run

# 2. Frontend (terminal 2)  ->  http://localhost:5173
cd frontend
npm install
npm run dev
```
Or press `Ctrl+Shift+P` → **Tasks: Run Task** → **Run CareerPilot (backend + frontend)**.

The first start creates a local H2 database in `backend/data/` and loads the demo dataset automatically.

| Role | Email | Password |
|---|---|---|
| Student (richest demo data) | `aarav@careerpilot.dev` | `Student@123` |
| Other students | `diya@`, `rohan@`, `ananya@`, `kabir@` … `@careerpilot.dev` | `Student@123` |
| Recruiter / admin | `admin@careerpilot.dev` | `Admin@123` |

## Dataset
`dataset/generate_dataset.py` builds a reproducible, fully fictional dataset: **67 skills, 16 companies, 38 jobs (incl. internships and closed postings), 14 student profiles, 48 applications** across every pipeline stage. It writes `backend/src/main/resources/seed/seed.json` (loaded by `DataSeeder` when the DB is empty) and CSVs in `dataset/` for Excel. To reset: stop the backend, delete `backend/data/`, start again. To regenerate: `python dataset/generate_dataset.py`.

## Run with Docker (PostgreSQL + backend + frontend)
```bash
cp .env.example .env      # set JWT_SECRET, optionally ANTHROPIC_API_KEY
docker compose up --build
# App: http://localhost:3000   API: http://localhost:8080
```

## AI features
Set `ANTHROPIC_API_KEY` (env var, backend only; never in frontend code) to use a real LLM. Optional `ANTHROPIC_MODEL`. Without a key, or if the API fails, the endpoints return a **rule-based fallback** and the UI shows a notice. LLM output is requested as JSON and validated before use. Resume text is treated as untrusted data in the prompt.

## Architecture
```
React (Vite) -> Controller -> Service -> Repository -> PostgreSQL / H2
                    |
                    +-> AiService -> LLM API -> validated JSON (or local fallback)
JWT filter + Spring Security guard every /api route. Business rules live only in the service layer.
```
`com.careerpilot`: `controller/ service/ repository/ entity/ dto/ security/ exception/ config/`

| Module | Why it exists | Key classes |
|---|---|---|
| Auth | Register/login, BCrypt hashing, stateless JWT, role checks | `AuthService`, `JwtService`, `JwtAuthFilter`, `SecurityConfig` |
| Matching | `Match % = matched required skills / total required × 100` using a `HashSet` (O(1) lookups, O(S+R) total); 0 required skills = 100% | `MatchService` |
| Eligibility | CGPA, graduation year, accepted degrees, deadline, open/closed | `EligibilityService` |
| Applications | One application per student per job (service check + DB unique constraint); validated status transitions | `ApplicationService`, `ApplicationStatus` |
| AI | Resume / JD analysis, interview questions, graceful fallback | `AiService` |

Status flow: `Applied → Shortlisted → Technical Interview → HR Interview → Selected`, with `Rejected` allowed from any open stage. Invalid jumps return **400** with the allowed options.

## Database design
`users(id, email, password_hash, role, created_at)` · `students(id, user_id, name, college, degree, cgpa, graduation_year, phone, projects, certifications)` · `skills(id, name)` · `student_skills(student_id, skill_id)` · `companies(id, name, location, website)` · `jobs(id, company_id, title, description, minimum_cgpa, location, salary_lpa, deadline, status, job_type, graduation_year, allowed_degrees)` · `job_skills(job_id, skill_id)` · `applications(id, student_id, job_id, status, applied_at, updated_at, UNIQUE(student_id, job_id))` · `resumes(...)` · `interview_sessions(...)` · `interview_questions(...)`

Relationships: User 1–1 Student · Student M–N Skill · Job M–N Skill · Company 1–N Job · Student 1–N Application · Job 1–N Application · Student 1–N Resume · InterviewSession 1–N InterviewQuestion. Indexes on `jobs.status`, `jobs.deadline`, `applications.status`.

## REST API
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET/PUT | `/api/students/profile` | student |
| POST | `/api/students/resume` (multipart PDF/TXT) | student |
| GET | `/api/skills` | any user |
| GET | `/api/jobs?q=&location=&sort=match\|deadline\|salary\|recent` | any user (students get match % + eligibility) |
| GET | `/api/jobs/{id}`, `/api/jobs/{id}/match` | any user / student |
| POST/PUT/DELETE | `/api/jobs`, `/api/jobs/{id}` | admin |
| GET/POST/PUT/DELETE | `/api/companies` | read: any, write: admin |
| POST | `/api/applications` | student |
| GET | `/api/applications`, `/api/applications/{id}` | student (own) / admin (all) |
| PUT | `/api/applications/{id}/status` | admin |
| POST | `/api/ai/resume-analysis`, `/api/ai/jd-analysis`, `/api/ai/interview` | authenticated |
| GET | `/api/dashboard/student`, `/api/dashboard/admin` | student / admin |
| GET | `/api/health` | public |

Import `postman/CareerPilot.postman_collection.json` into Postman. Run the login requests first; they save the JWTs.

## Tests
```bash
cd backend && mvn test
```
Covers: skill matching (full, partial, case, empty, zero-required, rounding), eligibility (CGPA, year, degree, deadline, closed), status transitions, duplicate application prevention (Mockito), ineligible apply, missing job (404), valid/invalid status updates.

## Environment variables
`DB_URL`, `DB_USER`, `DB_PASSWORD` (default: local H2) · `JWT_SECRET` (32+ chars) · `ANTHROPIC_API_KEY`, `ANTHROPIC_MODEL` · `CORS_ORIGINS` · `SEED_ENABLED` (default true) · frontend `VITE_API_URL` (default `/api`, proxied in dev)

## Suggested Git history
`1 project setup` → `2 database/entities` → `3 authentication` → `4 job management` → `5 applications` → `6 matching` → `7 frontend` → `8 AI` → `9 testing` → `10 Docker/docs`. Use branches `feature/auth`, `feature/jobs`, `feature/matching`, `feature/ai`. Never commit `.env`.

## Screenshots
Add to `docs/`: login, student dashboard, job list, job detail (match + eligibility), applications pipeline, resume analyzer, admin dashboard.

## Interview cheat sheet
- **Why DTOs?** Entities never leave the service layer, so the API shape is stable and no sensitive fields leak (e.g. `passwordHash`).
- **Where is HashSet used?** `MatchService`: student skills in a HashSet, then one O(1) check per required skill.
- **Why isn't matching done by AI?** It must be deterministic, explainable, testable and free; AI only coaches.
- **Preventing double applications?** Service check for a friendly 409, plus a DB unique constraint to stay safe under concurrent requests.
- **JWT flow?** Login → signed token with role claim → `JwtAuthFilter` validates on each request → `SecurityConfig` enforces roles. Stateless, so no server sessions.
- **When the AI API fails?** `AiService` catches the error and returns a labelled fallback; the app keeps working.
- **At 100k users?** Pagination, DB-level filtering and indexes, caching hot job lists, async AI calls with rate limits, read replicas.
- **N+1 problem?** `JobRepository.findAllDetailed()` uses `@EntityGraph` to fetch company and skills in one query.

## Future improvements
Pagination and server-side filtering, email notifications on status change, resume file storage, refresh tokens, rate limiting on AI endpoints, integration tests with Testcontainers.
