import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../../api.js'
import { Chip, Empty, ErrorBox, Loading, MatchRing, PageHeader, daysLeft, fmtDate, money, useFetch } from '../../components/ui.jsx'

export default function Jobs() {
  const [params] = useSearchParams()
  const [q, setQ] = useState(''); const [location, setLocation] = useState('')
  const [sort, setSort] = useState(params.get('sort') || 'match'); const [onlyEligible, setOnlyEligible] = useState(false)
  const [type, setType] = useState('')
  const { data, loading, error, reload } = useFetch(() => api.get(`/jobs?sort=${sort}`), [sort])
  const { data: profile } = useFetch(() => api.get('/students/profile'))
  const mine = useMemo(() => new Set((profile?.skills || []).map((s) => s.toLowerCase())), [profile])

  const locations = useMemo(() => [...new Set((data || []).map((j) => j.location).filter(Boolean))].sort(), [data])
  const list = useMemo(() => (data || []).filter((j) =>
    (!q || `${j.title} ${j.companyName} ${j.skills.join(' ')}`.toLowerCase().includes(q.toLowerCase())) &&
    (!location || j.location === location) && (!type || j.jobType === type) && (!onlyEligible || j.eligible)), [data, q, location, type, onlyEligible])

  return (
    <>
      <PageHeader title="Jobs" sub="Ranked by how well your skills fit. Green means you already have the skill." />
      <div className="card filters">
        <label className="f">Search<input placeholder="Role, company or skill" value={q} onChange={(e) => setQ(e.target.value)} /></label>
        <label className="f">Location<select value={location} onChange={(e) => setLocation(e.target.value)}><option value="">All locations</option>{locations.map((l) => <option key={l}>{l}</option>)}</select></label>
        <label className="f">Sort by<select value={sort} onChange={(e) => setSort(e.target.value)}><option value="match">Best match</option><option value="deadline">Deadline</option><option value="salary">Salary</option><option value="recent">Newest</option></select></label>
        <label className="f">Type<select value={type} onChange={(e) => setType(e.target.value)}><option value="">All</option><option>Full-time</option><option>Internship</option></select></label>
        <label className="row small" style={{ gridColumn: '1 / -1', fontWeight: 600 }}><input type="checkbox" style={{ width: 'auto' }} checked={onlyEligible} onChange={(e) => setOnlyEligible(e.target.checked)} />Show only jobs I am eligible for</label>
      </div>
      <ErrorBox message={error} onRetry={reload} />
      {loading ? <Loading /> : list.length === 0 ? <div className="card"><Empty title="No jobs match these filters">Try clearing a filter or searching a different skill.</Empty></div> : (
        <div className="col">
          {list.map((j) => {
            const left = daysLeft(j.deadline)
            return (
              <Link key={j.id} to={`/jobs/${j.id}`} className="job-row">
                <div>
                  <div className="row wrap" style={{ gap: 8 }}><h3>{j.title}</h3>{j.applied && <span className="pill APPLIED">Applied</span>}{!j.eligible && !j.applied && <span className="pill REJECTED">Not eligible</span>}</div>
                  <div className="job-meta"><span>{j.companyName}</span><span>{j.location}</span><span>{money(j)}</span><span>{j.jobType}</span><span>{left != null && left <= 7 ? `Closes in ${left} day${left === 1 ? '' : 's'}` : `Apply by ${fmtDate(j.deadline)}`}</span></div>
                  <div className="chips">{j.skills.map((s) => <Chip key={s} kind={mine.has(s.toLowerCase()) ? 'have' : ''}>{s}</Chip>)}</div>
                </div>
                <div className="job-side"><MatchRing value={j.matchPercentage} size={60} /></div>
              </Link>)
          })}
        </div>)}
    </>
  )
}
