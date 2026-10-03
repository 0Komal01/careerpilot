import { Link } from 'react-router-dom'
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { api } from '../../api.js'
import { useAuth } from '../../auth.jsx'
import { Empty, ErrorBox, Loading, MatchRing, PageHeader, fmtDate, useFetch } from '../../components/ui.jsx'

const COLORS = { Applied: '#2b59ff', Shortlisted: '#e19a1b', 'Technical Interview': '#7a52e0', 'HR Interview': '#a07cf0', Selected: '#14a06f', Rejected: '#db4b5a' }

export default function Dashboard() {
  const { user } = useAuth()
  const { data: d, loading, error, reload } = useFetch(() => api.get('/dashboard/student'))
  if (loading) return <Loading />
  if (error) return <ErrorBox message={error} onRetry={reload} />
  const chart = Object.entries(d.statusCounts).map(([name, value]) => ({ name, value }))
  return (
    <>
      <PageHeader title={`Hi ${user.name.split(' ')[0]}`} sub="Here is where your placement search stands today." />
      <div className="grid g4">
        <div className="card row"><MatchRing value={d.profileCompletion} size={64} /><div className="stat"><span className="l">Profile complete</span><Link to="/profile" className="small">{d.profileCompletion < 100 ? 'Finish profile' : 'View profile'}</Link></div></div>
        <div className="card stat"><span className="n">{d.totalApplications}</span><span className="l">Applications sent</span></div>
        <div className="card stat"><span className="n">{d.inPipeline}</span><span className="l">In interview pipeline</span></div>
        <div className="card stat"><span className="n" style={{ color: 'var(--mint)' }}>{d.selected}</span><span className="l">Offers (selected)</span></div>
      </div>
      <div className="grid" style={{ gridTemplateColumns: 'minmax(0,1fr) minmax(0,1.2fr)', marginTop: 16 }}>
        <div className="card">
          <h2>Application status</h2><p className="muted small">How your applications are spread across stages</p>
          {d.totalApplications === 0 ? <Empty title="No applications yet">Apply to a job and it will show up here.</Empty> : (
            <div style={{ height: 260, marginTop: 10 }}>
              <ResponsiveContainer><BarChart data={chart} layout="vertical" margin={{ left: 30, right: 12 }}>
                <CartesianGrid horizontal={false} stroke="#eef1f7" /><XAxis type="number" allowDecimals={false} /><YAxis type="category" dataKey="name" width={125} tick={{ fontSize: 12 }} />
                <Tooltip cursor={{ fill: '#f3f5fa' }} /><Bar dataKey="value" radius={[0, 6, 6, 0]}>{chart.map((c) => <Cell key={c.name} fill={COLORS[c.name]} />)}</Bar>
              </BarChart></ResponsiveContainer>
            </div>)}
        </div>
        <div className="card">
          <div className="row"><div><h2>Best matches for you</h2><p className="muted small">Open jobs you are eligible for, ranked by skill match</p></div><Link to="/jobs?sort=match" className="right small">See all jobs</Link></div>
          {d.topMatches.length === 0 ? <Empty title="No matches right now">Add more skills to your profile to see recommendations.</Empty> : (
            <div className="col" style={{ marginTop: 12, gap: 0 }}>
              {d.topMatches.map((j) => (
                <Link key={j.id} to={`/jobs/${j.id}`} className="row" style={{ padding: '12px 0', borderBottom: '1px solid #eef1f7', color: 'inherit' }}>
                  <MatchRing value={j.matchPercentage} size={46} stroke={5} />
                  <div><b>{j.title}</b><div className="muted small">{j.companyName} · {j.location} · apply by {fmtDate(j.deadline)}</div></div>
                </Link>))}
            </div>)}
        </div>
      </div>
    </>
  )
}
