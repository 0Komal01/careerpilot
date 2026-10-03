import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { Field } from '../components/ui.jsx'
import { Logo } from '../components/Layout.jsx'

function Hero() {
  return (
    <section className="auth-hero">
      <Logo />
      <div>
        <h1>Know where you stand before you apply.</h1>
        <p>See your skill match, check eligibility, track every application and practise interviews, all in one place.</p>
      </div>
      <svg className="route" width="420" height="220" viewBox="0 0 420 220" fill="none" aria-hidden="true">
        <path d="M10 200C90 190 110 90 190 100S300 170 360 60" stroke="#2b59ff" strokeWidth="3" strokeDasharray="2 9" strokeLinecap="round" />
        {[[10, 200], [190, 100], [360, 60]].map(([x, y], i) => <circle key={i} cx={x} cy={y} r="8" fill="#101a33" stroke={i === 2 ? '#14a06f' : '#2b59ff'} strokeWidth="4" />)}
      </svg>
    </section>
  )
}

export function Login() {
  const { login } = useAuth()
  const nav = useNavigate()
  const [f, setF] = useState({ email: '', password: '' })
  const [err, setErr] = useState(''); const [busy, setBusy] = useState(false)
  const submit = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true)
    try { const u = await login(f.email, f.password); nav(u.role === 'ADMIN' ? '/admin' : '/') }
    catch (x) { setErr(x.message) } finally { setBusy(false) }
  }
  return (
    <div className="auth"><Hero />
      <div className="auth-form">
        <form onSubmit={submit}>
          <div><h1>Welcome back</h1><p className="muted">Log in to continue to your workspace.</p></div>
          {err && <div className="alert err" role="alert">{err}</div>}
          <Field label="Email"><input type="email" required autoComplete="username" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Field>
          <Field label="Password"><input type="password" required autoComplete="current-password" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></Field>
          <button className="btn block" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
          <div className="demo">Demo accounts:<br />
            Student <button type="button" onClick={() => setF({ email: 'aarav@careerpilot.dev', password: 'Student@123' })}>aarav@careerpilot.dev</button><br />
            Recruiter <button type="button" onClick={() => setF({ email: 'admin@careerpilot.dev', password: 'Admin@123' })}>admin@careerpilot.dev</button>
          </div>
          <p className="muted small">New here? <Link to="/register">Create a student account</Link></p>
        </form>
      </div>
    </div>
  )
}

export function Register() {
  const { register } = useAuth()
  const nav = useNavigate()
  const [f, setF] = useState({ name: '', email: '', password: '' })
  const [err, setErr] = useState(''); const [fields, setFields] = useState({}); const [busy, setBusy] = useState(false)
  const submit = async (e) => {
    e.preventDefault(); setErr(''); setFields({}); setBusy(true)
    try { await register(f.name, f.email, f.password); nav('/profile') }
    catch (x) { setErr(x.message); setFields(x.fieldErrors || {}) } finally { setBusy(false) }
  }
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  return (
    <div className="auth"><Hero />
      <div className="auth-form">
        <form onSubmit={submit}>
          <div><h1>Create your account</h1><p className="muted">Takes a minute. You can finish your profile afterwards.</p></div>
          {err && <div className="alert err" role="alert">{err}</div>}
          <Field label="Full name" error={fields.name}><input required value={f.name} onChange={set('name')} className={fields.name ? 'invalid' : ''} /></Field>
          <Field label="Email" error={fields.email}><input type="email" required value={f.email} onChange={set('email')} className={fields.email ? 'invalid' : ''} /></Field>
          <Field label="Password (8+ characters)" error={fields.password}><input type="password" required minLength={8} value={f.password} onChange={set('password')} className={fields.password ? 'invalid' : ''} /></Field>
          <button className="btn block" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
          <p className="muted small">Already registered? <Link to="/login">Log in</Link></p>
        </form>
      </div>
    </div>
  )
}
