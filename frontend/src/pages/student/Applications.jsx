import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api.js'
import { Empty, ErrorBox, Loading, MatchRing, PageHeader, Pipeline, STATUS_LABEL, StatusPill, fmtDate, useFetch } from '../../components/ui.jsx'

export default function Applications() {
  const { data, loading, error, reload } = useFetch(() => api.get('/applications'))
  const [filter, setFilter] = useState('')
  if (loading) return <Loading />
  const list = (data || []).filter((a) => !filter || a.status === filter)
  return (
    <>
      <PageHeader title="My applications" sub="Every application and where it stands.">
        <select value={filter} onChange={(e) => setFilter(e.target.value)} style={{ width: 200 }} aria-label="Filter by status">
          <option value="">All statuses</option>{Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </PageHeader>
      <ErrorBox message={error} onRetry={reload} />
      {list.length === 0 ? <div className="card"><Empty title={filter ? 'Nothing at this stage' : 'No applications yet'}>{!filter && <>Find a role on the <Link to="/jobs">jobs page</Link> and apply.</>}</Empty></div> : (
        <div className="col">
          {list.map((a) => (
            <div key={a.id} className="card" style={{ display: 'grid', gridTemplateColumns: 'minmax(180px,1fr) minmax(260px,1.4fr) auto', gap: 24, alignItems: 'center' }}>
              <div><Link to={`/jobs/${a.jobId}`}><b>{a.jobTitle}</b></Link><div className="muted small">{a.companyName}</div><div className="muted small">Applied {fmtDate(a.appliedAt)}</div></div>
              {a.status === 'REJECTED' ? <div><StatusPill status="REJECTED" /><div className="muted small" style={{ marginTop: 6 }}>Not taken forward. Keep going, other roles may fit better.</div></div> : <Pipeline status={a.status} />}
              <div className="row"><MatchRing value={a.matchPercentage} size={46} stroke={5} /></div>
            </div>))}
        </div>)}
    </>
  )
}
