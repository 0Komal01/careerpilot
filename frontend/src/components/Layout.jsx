import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

const I = (d) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d={d} /></svg>
const icons = {
  home: I('M3 11 12 3l9 8M5 10v10h5v-6h4v6h5V10'), user: I('M20 21a8 8 0 0 0-16 0M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8'),
  jobs: I('M3 8h18v12H3zM9 8V5h6v3'), apps: I('M9 11l3 3 8-8M20 12v7a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h9'),
  resume: I('M6 3h9l4 4v14H6zM14 3v5h5M9 13h7M9 17h7'), jd: I('M4 5h16M4 10h10M4 15h16M4 20h10'),
  chat: I('M21 12a8 8 0 0 1-11.5 7.2L4 20l1-4.5A8 8 0 1 1 21 12Z'), company: I('M4 21V5l8-2v18M12 9h8v12M7 9h2M7 13h2M7 17h2'),
}
const STUDENT = [['/', 'Dashboard', 'home'], ['/jobs', 'Jobs', 'jobs'], ['/applications', 'My applications', 'apps'], ['/profile', 'Profile', 'user'],
  ['/resume', 'Resume analyzer', 'resume'], ['/jd', 'JD analyzer', 'jd'], ['/interview', 'Interview prep', 'chat']]
const ADMIN = [['/admin', 'Dashboard', 'home'], ['/admin/companies', 'Companies', 'company'], ['/admin/jobs', 'Jobs', 'jobs'], ['/admin/applications', 'Applications', 'apps']]

export const Logo = () => (
  <span className="brand"><span className="brand-mark"><svg width="18" height="18" viewBox="0 0 32 32"><path d="M5 16.5 27 6l-7.5 21-4-8.8L5 16.5Z" fill="#fff" /></svg></span>CareerPilot</span>
)

export default function Layout() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const [open, setOpen] = useState(false)
  const items = user.role === 'ADMIN' ? ADMIN : STUDENT
  return (
    <div className="shell">
      <div className="mobilebar"><button className="btn sm ghost" style={{ color: '#fff', borderColor: '#33406a' }} onClick={() => setOpen(!open)} aria-label="Toggle menu">Menu</button>CareerPilot</div>
      <aside className={`sidebar ${open ? 'open' : ''}`} onClick={() => setOpen(false)}>
        <Logo />
        <nav className="nav">
          <div className="nav-label">{user.role === 'ADMIN' ? 'Recruiter workspace' : 'Student workspace'}</div>
          {items.map(([to, label, ic]) => (
            <NavLink key={to} to={to} end={to === '/' || to === '/admin'}>{icons[ic]}{label}</NavLink>
          ))}
        </nav>
        <div className="me"><b>{user.name}</b><span>{user.email}</span>
          <button className="btn sm ghost" style={{ color: '#fff', borderColor: '#33406a' }} onClick={() => { logout(); nav('/login') }}>Log out</button>
        </div>
      </aside>
      <main className="main"><Outlet /></main>
    </div>
  )
}
