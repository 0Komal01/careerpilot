import { useEffect, useState } from 'react'
import { api } from '../../api.js'
import { ErrorBox, Field, Loading, MatchRing, PageHeader, TagInput, fmtDate, useFetch } from '../../components/ui.jsx'

export default function Profile() {
  const { data, loading, error, reload } = useFetch(() => api.get('/students/profile'))
  const { data: skillList } = useFetch(() => api.get('/skills'))
  const [f, setF] = useState(null)
  const [msg, setMsg] = useState(null); const [fields, setFields] = useState({}); const [busy, setBusy] = useState(false)
  useEffect(() => { if (data) setF({ ...data, cgpa: data.cgpa || '', graduationYear: data.graduationYear || '' }) }, [data])
  if (loading || !f) return error ? <ErrorBox message={error} onRetry={reload} /> : <Loading />
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  const save = async (e) => {
    e.preventDefault(); setMsg(null); setFields({}); setBusy(true)
    try {
      await api.put('/students/profile', { ...f, cgpa: f.cgpa === '' ? null : Number(f.cgpa), graduationYear: f.graduationYear === '' ? null : Number(f.graduationYear) })
      setMsg({ t: 'ok', m: 'Profile saved' }); reload()
    } catch (x) { setMsg({ t: 'err', m: x.message }); setFields(x.fieldErrors || {}) } finally { setBusy(false) }
  }
  const upload = async (e) => {
    const file = e.target.files[0]; if (!file) return
    const form = new FormData(); form.append('file', file)
    try { await api.upload('/students/resume', form); setMsg({ t: 'ok', m: 'Resume uploaded and text extracted' }); reload() }
    catch (x) { setMsg({ t: 'err', m: x.message }) }
    e.target.value = ''
  }
  return (
    <>
      <PageHeader title="Your profile" sub="A complete profile gives you more accurate eligibility checks and matches.">
        <div className="row card tight"><MatchRing value={data.completion} size={44} stroke={5} /><span className="small"><b>Profile strength</b><br /><span className="muted">{data.email}</span></span></div>
      </PageHeader>
      {msg && <div className={`alert ${msg.t}`} role="status" style={{ marginBottom: 16 }}>{msg.m}</div>}
      <form onSubmit={save} className="col" style={{ gap: 16 }}>
        <div className="card">
          <h2>Education</h2>
          <div className="grid g2" style={{ marginTop: 14 }}>
            <Field label="Full name" error={fields.name}><input required value={f.name || ''} onChange={set('name')} /></Field>
            <Field label="Phone"><input value={f.phone || ''} onChange={set('phone')} placeholder="+91 …" /></Field>
            <Field label="College"><input value={f.college || ''} onChange={set('college')} /></Field>
            <Field label="Degree"><input value={f.degree || ''} onChange={set('degree')} placeholder="B.Tech Computer Engineering" /></Field>
            <Field label="CGPA (0 to 10)" error={fields.cgpa}><input type="number" step="0.01" min="0" max="10" value={f.cgpa} onChange={set('cgpa')} className={fields.cgpa ? 'invalid' : ''} /></Field>
            <Field label="Graduation year" error={fields.graduationYear}><input type="number" min="2000" max="2100" value={f.graduationYear} onChange={set('graduationYear')} /></Field>
          </div>
        </div>
        <div className="card">
          <h2>Skills</h2><p className="muted small">Job matches are calculated from these. Add at least 3.</p>
          <div style={{ marginTop: 12 }}><TagInput value={f.skills} onChange={(skills) => setF({ ...f, skills })} suggestions={skillList || []} /></div>
        </div>
        <div className="card">
          <h2>Projects and certifications</h2>
          <div className="grid g2" style={{ marginTop: 14 }}>
            <Field label="Projects"><textarea value={f.projects || ''} onChange={set('projects')} placeholder="One project per line, with the outcome" /></Field>
            <Field label="Certifications"><textarea value={f.certifications || ''} onChange={set('certifications')} /></Field>
          </div>
        </div>
        <div className="row"><button className="btn" disabled={busy}>{busy ? 'Saving…' : 'Save profile'}</button></div>
      </form>
      <div className="card" style={{ marginTop: 16 }}>
        <h2>Resume</h2>
        <p className="muted small">Upload a text-based PDF or TXT file. We store the extracted text so the analyzer can use it.</p>
        <div className="row wrap" style={{ marginTop: 12 }}>
          <input type="file" accept=".pdf,.txt,.md" onChange={upload} style={{ maxWidth: 320 }} />
          {data.latestResume && <span className="small muted">Current: <b>{data.latestResume.fileName}</b> · {fmtDate(data.latestResume.uploadedAt)}</span>}
        </div>
      </div>
    </>
  )
}
