import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api.js'
import { Chip, ErrorBox, Field, MatchRing, PageHeader, useFetch } from '../../components/ui.jsx'

const Notice = ({ r }) => r?.notice ? <div className="alert warn">{r.notice}</div> : null
const List = ({ items }) => items?.length ? <ul style={{ margin: '8px 0 0', paddingLeft: 18, lineHeight: 1.7 }}>{items.map((x) => <li key={x}>{x}</li>)}</ul> : <p className="muted small">None found.</p>

export function ResumeAnalyzer() {
  const { data: profile } = useFetch(() => api.get('/students/profile'))
  const [text, setText] = useState(''); const [role, setRole] = useState('')
  const [res, setRes] = useState(null); const [err, setErr] = useState(''); const [busy, setBusy] = useState(false)
  useEffect(() => { if (profile?.latestResume && !text) setText(profile.latestResume.extractedText) }, [profile]) // eslint-disable-line
  const run = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true); setRes(null)
    try { setRes(await api.post('/ai/resume-analysis', { text, targetRole: role })) } catch (x) { setErr(x.message) } finally { setBusy(false) }
  }
  return (
    <>
      <PageHeader title="Resume analyzer" sub="Paste your resume (or use your uploaded one) and get skills, gaps and concrete fixes." />
      <div className="grid" style={{ gridTemplateColumns: 'minmax(0,1fr) minmax(0,1fr)', alignItems: 'start' }}>
        <form className="card col" onSubmit={run}>
          <Field label="Target role (optional)"><input value={role} onChange={(e) => setRole(e.target.value)} placeholder="e.g. Java backend developer" /></Field>
          <Field label="Resume text"><textarea style={{ minHeight: 320 }} required value={text} onChange={(e) => setText(e.target.value)} placeholder="Paste resume text here" /></Field>
          <ErrorBox message={err} />
          <button className="btn" disabled={busy || text.trim().length < 30}>{busy ? 'Analyzing…' : 'Analyze resume'}</button>
        </form>
        {res ? (
          <div className="col">
            <Notice r={res} />
            <div className="card row"><MatchRing value={res.score} size={84} stroke={8} /><div><h2>Resume score</h2><p className="muted small">{res.scoreExplanation}</p></div></div>
            <div className="card"><h3>Skills detected</h3><div className="chips" style={{ marginTop: 8 }}>{res.detectedSkills.length ? res.detectedSkills.map((s) => <Chip key={s} kind="have">{s}</Chip>) : <span className="muted small">None recognised</span>}</div></div>
            <div className="card"><h3>Gaps for this role</h3><div className="chips" style={{ marginTop: 8 }}>{res.missingSkills.length ? res.missingSkills.map((s) => <Chip key={s} kind="miss">{s}</Chip>) : <span className="muted small">No obvious gaps</span>}</div></div>
            <div className="card"><h3>Strengths</h3><List items={res.strengths} /></div>
            <div className="card"><h3>What to improve</h3><List items={res.suggestions} /></div>
          </div>
        ) : <div className="card muted">Your analysis will appear here. The core job match on the Jobs page never depends on AI; this tool is for coaching only.</div>}
      </div>
    </>
  )
}

export function JdAnalyzer() {
  const { data: profile } = useFetch(() => api.get('/students/profile'))
  const [text, setText] = useState(''); const [res, setRes] = useState(null); const [err, setErr] = useState(''); const [busy, setBusy] = useState(false)
  const run = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true); setRes(null)
    try { setRes(await api.post('/ai/jd-analysis', { text })) } catch (x) { setErr(x.message) } finally { setBusy(false) }
  }
  // Same idea as the backend: a Set of my skills, then check each required skill.
  const mine = useMemo(() => new Set((profile?.skills || []).map((s) => s.toLowerCase())), [profile])
  const fit = res && res.requiredSkills.length ? Math.round(100 * res.requiredSkills.filter((s) => mine.has(s.toLowerCase())).length / res.requiredSkills.length) : null
  return (
    <>
      <PageHeader title="JD analyzer" sub="Paste any job description to pull out the role, skills and requirements, then compare with your profile." />
      <div className="grid" style={{ gridTemplateColumns: 'minmax(0,1fr) minmax(0,1fr)', alignItems: 'start' }}>
        <form className="card col" onSubmit={run}>
          <Field label="Job description"><textarea style={{ minHeight: 340 }} required value={text} onChange={(e) => setText(e.target.value)} placeholder="Paste the full job description" /></Field>
          <ErrorBox message={err} />
          <button className="btn" disabled={busy || text.trim().length < 20}>{busy ? 'Analyzing…' : 'Analyze job description'}</button>
        </form>
        {res ? (
          <div className="col">
            <Notice r={res} />
            <div className="card"><h2>{res.role || 'Role not detected'}</h2>
              <div className="grid g2" style={{ marginTop: 12 }}><div><div className="muted small">Experience</div><b>{res.experience}</b></div><div><div className="muted small">Education</div><b>{res.education}</b></div></div></div>
            {fit != null && <div className="card row"><MatchRing value={fit} size={64} /><div><h3>Your fit for this description</h3><p className="muted small">Based on the required skills found and the skills on your profile.</p></div></div>}
            <div className="card"><h3>Required skills</h3><div className="chips" style={{ marginTop: 8 }}>{res.requiredSkills.length ? res.requiredSkills.map((s) => <Chip key={s} kind={mine.has(s.toLowerCase()) ? 'have' : 'miss'}>{s}</Chip>) : <span className="muted small">None found</span>}</div></div>
            <div className="card"><h3>Nice to have</h3><div className="chips" style={{ marginTop: 8 }}>{res.preferredSkills.length ? res.preferredSkills.map((s) => <Chip key={s}>{s}</Chip>) : <span className="muted small">None found</span>}</div></div>
            <div className="card"><h3>Other requirements</h3><List items={res.otherRequirements} /></div>
          </div>
        ) : <div className="card muted">Extracted requirements will appear here.</div>}
      </div>
    </>
  )
}

const TOPICS = ['Java', 'Spring Boot', 'SQL', 'DSA', 'React', 'System Design', 'Testing', 'HR']
export function Interview() {
  const [role, setRole] = useState('Java Backend Developer'); const [topics, setTopics] = useState(['Java', 'Spring Boot', 'SQL', 'HR'])
  const [difficulty, setDifficulty] = useState('Medium'); const [count, setCount] = useState(8)
  const [res, setRes] = useState(null); const [err, setErr] = useState(''); const [busy, setBusy] = useState(false)
  const toggle = (t) => setTopics(topics.includes(t) ? topics.filter((x) => x !== t) : [...topics, t])
  const run = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true)
    try { setRes(await api.post('/ai/interview', { targetRole: role, topics, difficulty, count: Number(count) })) } catch (x) { setErr(x.message) } finally { setBusy(false) }
  }
  return (
    <>
      <PageHeader title="Interview preparation" sub="Practise with role-specific questions. Try answering before you open the hints." />
      <form className="card col" onSubmit={run} style={{ marginBottom: 16 }}>
        <div className="grid g3">
          <Field label="Target role"><input required value={role} onChange={(e) => setRole(e.target.value)} /></Field>
          <Field label="Difficulty"><select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}><option>Easy</option><option>Medium</option><option>Hard</option></select></Field>
          <Field label="Number of questions"><input type="number" min="1" max="15" value={count} onChange={(e) => setCount(e.target.value)} /></Field>
        </div>
        <div><div className="small" style={{ fontWeight: 600, marginBottom: 8 }}>Topics</div>
          <div className="chips">{TOPICS.map((t) => <button type="button" key={t} onClick={() => toggle(t)} className={`chip ${topics.includes(t) ? 'blue' : ''}`} style={{ cursor: 'pointer' }} aria-pressed={topics.includes(t)}>{t}</button>)}</div></div>
        <ErrorBox message={err} />
        <div><button className="btn" disabled={busy}>{busy ? 'Generating…' : 'Generate questions'}</button></div>
      </form>
      {res && (
        <div className="col"><Notice r={res} />
          {res.questions.map((q, i) => (
            <details key={i} className="q-card">
              <summary><span className="chip blue">{q.category}</span><span style={{ flex: 1 }}>{q.question}</span><span className="muted small">{q.difficulty}</span></summary>
              <div className="small muted" style={{ marginTop: 10 }}>Good answers usually cover:</div>
              <ul>{q.expectedPoints.map((p) => <li key={p}>{p}</li>)}</ul>
            </details>))}
        </div>)}
    </>
  )
}
