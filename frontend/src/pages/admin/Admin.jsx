import { useMemo, useState } from 'react'
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, Legend } from 'recharts'
import { api } from '../../api.js'
import { Chip, Empty, ErrorBox, Field, Loading, Modal, PageHeader, STATUS_LABEL, StatusPill, TagInput, fmtDate, money, useFetch } from '../../components/ui.jsx'

const COLORS = { Applied: '#2b59ff', Shortlisted: '#e19a1b', 'Technical Interview': '#7a52e0', 'HR Interview': '#a07cf0', Selected: '#14a06f', Rejected: '#db4b5a' }

export function AdminDashboard() {
  const { data: d, loading, error, reload } = useFetch(() => api.get('/dashboard/admin'))
  if (loading) return <Loading />
  if (error) return <ErrorBox message={error} onRetry={reload} />
  const pie = Object.entries(d.statusCounts).filter(([, v]) => v > 0).map(([name, value]) => ({ name, value }))
  const rate = d.applications ? Math.round((100 * d.selected) / d.applications) : 0
  return (
    <>
      <PageHeader title="Placement overview" sub="Live numbers across companies, jobs and applications." />
      <div className="grid g4">
        {[['Companies', d.companies], ['Open jobs', `${d.openJobs} / ${d.jobs}`], ['Students', d.students], ['Applications', d.applications]].map(([l, n]) => <div key={l} className="card stat"><span className="n">{n}</span><span className="l">{l}</span></div>)}
      </div>
      <div className="grid g2" style={{ marginTop: 16 }}>
        <div className="card"><h2>Pipeline</h2><p className="muted small">{rate}% of applications end in a selection</p>
          <div style={{ height: 280 }}><ResponsiveContainer><PieChart><Pie data={pie} dataKey="value" nameKey="name" innerRadius={62} outerRadius={96} paddingAngle={2}>{pie.map((p) => <Cell key={p.name} fill={COLORS[p.name]} />)}</Pie><Tooltip /><Legend /></PieChart></ResponsiveContainer></div></div>
        <div className="card"><h2>Most applied-to jobs</h2><p className="muted small">Top six by number of applications</p>
          <div style={{ height: 280 }}><ResponsiveContainer><BarChart data={d.topJobs} layout="vertical" margin={{ left: 30 }}><CartesianGrid horizontal={false} stroke="#eef1f7" /><XAxis type="number" allowDecimals={false} /><YAxis type="category" dataKey="title" width={140} tick={{ fontSize: 12 }} /><Tooltip cursor={{ fill: '#f3f5fa' }} /><Bar dataKey="applications" fill="#2b59ff" radius={[0, 6, 6, 0]} /></BarChart></ResponsiveContainer></div></div>
      </div>
    </>
  )
}

export function Companies() {
  const { data, loading, error, reload } = useFetch(() => api.get('/companies'))
  const [edit, setEdit] = useState(null); const [msg, setMsg] = useState('')
  const [err, setErr] = useState(''); const [fields, setFields] = useState({})
  const save = async (e) => {
    e.preventDefault(); setErr(''); setFields({})
    try { edit.id ? await api.put(`/companies/${edit.id}`, edit) : await api.post('/companies', edit); setEdit(null); reload() }
    catch (x) { setErr(x.message); setFields(x.fieldErrors || {}) }
  }
  const remove = async (c) => {
    if (!confirm(`Delete ${c.name}?`)) return
    try { await api.del(`/companies/${c.id}`); setMsg(''); reload() } catch (x) { setMsg(x.message) }
  }
  return (
    <>
      <PageHeader title="Companies" sub="Employers that post jobs on CareerPilot."><button className="btn" onClick={() => { setErr(''); setEdit({ name: '', location: '', website: '' }) }}>Add company</button></PageHeader>
      <ErrorBox message={error || msg} onRetry={error ? reload : null} />
      {loading ? <Loading /> : !data?.length ? <div className="card"><Empty title="No companies yet">Add the first one to start posting jobs.</Empty></div> : (
        <div className="card table-wrap"><table><thead><tr><th>Company</th><th>Location</th><th>Website</th><th>Jobs</th><th /></tr></thead><tbody>
          {data.map((c) => <tr key={c.id}><td><b>{c.name}</b></td><td>{c.location}</td><td className="muted">{c.website}</td><td>{c.jobs}</td>
            <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}><button className="btn sm ghost" onClick={() => { setErr(''); setEdit({ ...c }) }}>Edit</button>{' '}<button className="btn sm danger" onClick={() => remove(c)}>Delete</button></td></tr>)}
        </tbody></table></div>)}
      {edit && <Modal title={edit.id ? 'Edit company' : 'Add company'} onClose={() => setEdit(null)}>
        <form className="col" onSubmit={save}>
          <Field label="Name" error={fields.name}><input required value={edit.name} onChange={(e) => setEdit({ ...edit, name: e.target.value })} /></Field>
          <Field label="Location"><input value={edit.location || ''} onChange={(e) => setEdit({ ...edit, location: e.target.value })} /></Field>
          <Field label="Website"><input value={edit.website || ''} onChange={(e) => setEdit({ ...edit, website: e.target.value })} /></Field>
          <ErrorBox message={err} /><button className="btn">Save company</button>
        </form></Modal>}
    </>
  )
}

const blankJob = { companyId: '', title: '', description: '', minimumCgpa: 6.5, location: '', salaryLpa: '', deadline: '', status: 'OPEN', jobType: 'Full-time', graduationYear: '', allowedDegrees: 'B.Tech,B.E.,MCA', skills: [] }

export function JobManagement() {
  const { data: jobs, loading, error, reload } = useFetch(() => api.get('/jobs?sort=recent'))
  const { data: companies } = useFetch(() => api.get('/companies'))
  const { data: skillList } = useFetch(() => api.get('/skills'))
  const [edit, setEdit] = useState(null); const [err, setErr] = useState(''); const [fields, setFields] = useState({}); const [msg, setMsg] = useState('')
  const [q, setQ] = useState('')
  const rows = useMemo(() => (jobs || []).filter((j) => !q || `${j.title} ${j.companyName}`.toLowerCase().includes(q.toLowerCase())), [jobs, q])
  const set = (k) => (e) => setEdit({ ...edit, [k]: e.target.value })
  const save = async (e) => {
    e.preventDefault(); setErr(''); setFields({})
    const body = { ...edit, companyId: Number(edit.companyId), minimumCgpa: Number(edit.minimumCgpa), salaryLpa: edit.salaryLpa === '' ? null : Number(edit.salaryLpa), graduationYear: edit.graduationYear === '' ? null : Number(edit.graduationYear), deadline: edit.deadline || null }
    try { edit.id ? await api.put(`/jobs/${edit.id}`, body) : await api.post('/jobs', body); setEdit(null); reload() }
    catch (x) { setErr(x.message); setFields(x.fieldErrors || {}) }
  }
  const remove = async (j) => {
    if (!confirm(`Delete "${j.title}"?`)) return
    try { await api.del(`/jobs/${j.id}`); setMsg(''); reload() } catch (x) { setMsg(x.message) }
  }
  const toggle = async (j) => {
    try { await api.put(`/jobs/${j.id}`, { ...j, status: j.status === 'OPEN' ? 'CLOSED' : 'OPEN' }); reload() } catch (x) { setMsg(x.message) }
  }
  return (
    <>
      <PageHeader title="Jobs" sub="Create postings, set required skills and eligibility rules.">
        <input placeholder="Search jobs" value={q} onChange={(e) => setQ(e.target.value)} style={{ width: 200 }} />
        <button className="btn" onClick={() => { setErr(''); setFields({}); setEdit({ ...blankJob, companyId: companies?.[0]?.id || '' }) }}>Add job</button>
      </PageHeader>
      <ErrorBox message={error || msg} onRetry={error ? reload : null} />
      {loading ? <Loading /> : !rows.length ? <div className="card"><Empty title="No jobs found">Post a job to get started.</Empty></div> : (
        <div className="card table-wrap"><table><thead><tr><th>Role</th><th>Skills</th><th>Min CGPA</th><th>Deadline</th><th>Applicants</th><th>Status</th><th /></tr></thead><tbody>
          {rows.map((j) => <tr key={j.id}>
            <td><b>{j.title}</b><div className="muted small">{j.companyName} · {j.location} · {money(j)}</div></td>
            <td style={{ maxWidth: 260 }}><div className="chips">{j.skills.slice(0, 4).map((s) => <Chip key={s}>{s}</Chip>)}{j.skills.length > 4 && <Chip>+{j.skills.length - 4}</Chip>}</div></td>
            <td>{j.minimumCgpa.toFixed(1)}</td><td>{fmtDate(j.deadline)}</td><td>{j.applicants}</td><td><StatusPill status={j.status} /></td>
            <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
              <button className="btn sm ghost" onClick={() => { setErr(''); setFields({}); setEdit({ ...j, salaryLpa: j.salaryLpa ?? '', graduationYear: j.graduationYear ?? '', allowedDegrees: j.allowedDegrees || '' }) }}>Edit</button>{' '}
              <button className="btn sm ghost" onClick={() => toggle(j)}>{j.status === 'OPEN' ? 'Close' : 'Reopen'}</button>{' '}
              <button className="btn sm danger" onClick={() => remove(j)}>Delete</button></td></tr>)}
        </tbody></table></div>)}
      {edit && <Modal title={edit.id ? 'Edit job' : 'Add job'} onClose={() => setEdit(null)}>
        <form className="col" onSubmit={save}>
          <div className="grid g2">
            <Field label="Company" error={fields.companyId}><select required value={edit.companyId} onChange={set('companyId')}><option value="">Choose…</option>{(companies || []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select></Field>
            <Field label="Job title" error={fields.title}><input required value={edit.title} onChange={set('title')} className={fields.title ? 'invalid' : ''} /></Field>
            <Field label="Location"><input value={edit.location || ''} onChange={set('location')} /></Field>
            <Field label="Type"><select value={edit.jobType} onChange={set('jobType')}><option>Full-time</option><option>Internship</option></select></Field>
            <Field label="Pay (LPA, or yearly equivalent for internships)"><input type="number" step="0.1" min="0" value={edit.salaryLpa} onChange={set('salaryLpa')} /></Field>
            <Field label="Application deadline"><input type="date" value={edit.deadline || ''} onChange={set('deadline')} /></Field>
            <Field label="Minimum CGPA" error={fields.minimumCgpa}><input type="number" step="0.1" min="0" max="10" value={edit.minimumCgpa} onChange={set('minimumCgpa')} /></Field>
            <Field label="Graduation year (optional)"><input type="number" value={edit.graduationYear} onChange={set('graduationYear')} placeholder="e.g. 2027" /></Field>
          </div>
          <Field label="Accepted degrees (comma separated)"><input value={edit.allowedDegrees || ''} onChange={set('allowedDegrees')} /></Field>
          <Field label="Required skills"><TagInput value={edit.skills} onChange={(skills) => setEdit({ ...edit, skills })} suggestions={skillList || []} /></Field>
          <Field label="Description"><textarea value={edit.description || ''} onChange={set('description')} /></Field>
          <ErrorBox message={err} /><button className="btn">Save job</button>
        </form></Modal>}
    </>
  )
}

export function AdminApplications() {
  const { data, loading, error, reload } = useFetch(() => api.get('/applications'))
  const [q, setQ] = useState(''); const [status, setStatus] = useState(''); const [msg, setMsg] = useState('')
  const rows = useMemo(() => (data || []).filter((a) => (!status || a.status === status) && (!q || `${a.studentName} ${a.studentEmail} ${a.jobTitle} ${a.companyName}`.toLowerCase().includes(q.toLowerCase()))), [data, q, status])
  const move = async (a, next) => {
    if (!next) return
    try { await api.put(`/applications/${a.id}/status`, { status: next }); setMsg(''); reload() } catch (x) { setMsg(x.message) }
  }
  return (
    <>
      <PageHeader title="Applications" sub="Move candidates through the pipeline. Only valid next steps are offered.">
        <input placeholder="Search student, job or company" value={q} onChange={(e) => setQ(e.target.value)} style={{ width: 260 }} />
        <select value={status} onChange={(e) => setStatus(e.target.value)} style={{ width: 190 }} aria-label="Filter by status"><option value="">All statuses</option>{Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select>
      </PageHeader>
      <ErrorBox message={error || msg} onRetry={error ? reload : null} />
      {loading ? <Loading /> : !rows.length ? <div className="card"><Empty title="No applications match">Try a different search or status.</Empty></div> : (
        <div className="card table-wrap"><table><thead><tr><th>Student</th><th>Job</th><th>CGPA</th><th>Match</th><th>Applied</th><th>Status</th><th>Move to</th></tr></thead><tbody>
          {rows.map((a) => <tr key={a.id}>
            <td><b>{a.studentName}</b><div className="muted small">{a.studentEmail}</div></td>
            <td>{a.jobTitle}<div className="muted small">{a.companyName}</div></td><td>{a.studentCgpa.toFixed(2)}</td><td>{Math.round(a.matchPercentage)}%</td><td>{fmtDate(a.appliedAt)}</td>
            <td><StatusPill status={a.status} /></td>
            <td>{a.allowedNext.length === 0 ? <span className="muted small">Final</span> : (
              <select value="" onChange={(e) => move(a, e.target.value)} aria-label={`Move ${a.studentName} to`} style={{ width: 170 }}><option value="">Choose…</option>{a.allowedNext.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}</select>)}</td>
          </tr>)}
        </tbody></table></div>)}
    </>
  )
}
