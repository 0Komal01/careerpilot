import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../../api.js'
import { Chip, ErrorBox, Loading, MatchRing, Pipeline, fmtDate, money, useFetch } from '../../components/ui.jsx'

export default function JobDetail() {
  const { id } = useParams()
  const { data: job, loading, error, reload } = useFetch(() => api.get(`/jobs/${id}`), [id])
  const { data: match, reload: reloadMatch } = useFetch(() => api.get(`/jobs/${id}/match`), [id])
  const { data: apps, reload: reloadApps } = useFetch(() => api.get('/applications'), [id])
  const [msg, setMsg] = useState(null); const [busy, setBusy] = useState(false)
  if (loading || !match) return error ? <ErrorBox message={error} onRetry={reload} /> : <Loading />
  const mine = apps?.find((a) => a.jobId === job.id)
  const apply = async () => {
    setBusy(true); setMsg(null)
    try { await api.post('/applications', { jobId: job.id }); setMsg({ t: 'ok', m: 'Application submitted. Good luck!' }); reload(); reloadMatch(); reloadApps() }
    catch (x) { setMsg({ t: 'err', m: x.message }) } finally { setBusy(false) }
  }
  const el = match.eligibility
  return (
    <>
      <p className="small"><Link to="/jobs">← All jobs</Link></p>
      <div className="page-head">
        <div><h1>{job.title}</h1><p>{job.companyName} · {job.location} · {job.jobType}</p></div>
        <div className="right"><button className="btn" disabled={busy || job.applied || !el.eligible} onClick={apply}>{job.applied ? 'Already applied' : busy ? 'Submitting…' : 'Apply now'}</button></div>
      </div>
      {msg && <div className={`alert ${msg.t}`} role="status" style={{ marginBottom: 16 }}>{msg.m}</div>}
      <div className="grid" style={{ gridTemplateColumns: 'minmax(0,1.5fr) minmax(0,1fr)' }}>
        <div className="col">
          <div className="card"><h2>About the role</h2><p style={{ whiteSpace: 'pre-line', lineHeight: 1.65 }}>{job.description}</p>
            <div className="grid g3" style={{ marginTop: 12 }}>
              <div><div className="muted small">Pay</div><b>{money(job)}</b></div>
              <div><div className="muted small">Apply by</div><b>{fmtDate(job.deadline)}</b></div>
              <div><div className="muted small">Minimum CGPA</div><b>{job.minimumCgpa.toFixed(1)}</b></div>
            </div>
          </div>
          {mine && <div className="card"><h2>Your application</h2><p className="muted small">Applied on {fmtDate(mine.appliedAt)}</p><div style={{ marginTop: 14 }}><Pipeline status={mine.status} /></div>
            {mine.status === 'REJECTED' && <p className="alert err" style={{ marginTop: 14 }}>This application was not taken forward.</p>}</div>}
        </div>
        <div className="col">
          <div className="card">
            <div className="row"><MatchRing value={match.matchPercentage} size={84} stroke={8} /><div><h2>Skill match</h2><p className="muted small">{match.matchedSkills.length} of {match.matchedSkills.length + match.missingSkills.length} required skills</p></div></div>
            <div style={{ marginTop: 16 }}>
              {match.matchedSkills.length > 0 && <><div className="small muted" style={{ marginBottom: 6 }}>You have</div><div className="chips">{match.matchedSkills.map((s) => <Chip key={s} kind="have">{s}</Chip>)}</div></>}
              {match.missingSkills.length > 0 && <><div className="small muted" style={{ margin: '14px 0 6px' }}>To learn</div><div className="chips">{match.missingSkills.map((s) => <Chip key={s} kind="miss">{s}</Chip>)}</div></>}
              {match.matchedSkills.length + match.missingSkills.length === 0 && <p className="muted small">This job lists no required skills.</p>}
            </div>
          </div>
          <div className="card">
            <h2>{el.eligible ? 'You are eligible' : 'Not eligible yet'}</h2>
            <ul className="checklist" style={{ marginTop: 12 }}>
              {el.reasons.map((r) => <li key={r}><span className="dot ok">✓</span>{r}</li>)}
              {el.missingRequirements.map((r) => <li key={r}><span className="dot no">!</span>{r}</li>)}
            </ul>
          </div>
        </div>
      </div>
    </>
  )
}
