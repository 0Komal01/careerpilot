import { useEffect, useState, useCallback } from 'react'

export const STATUS_LABEL = { APPLIED: 'Applied', SHORTLISTED: 'Shortlisted', TECHNICAL_INTERVIEW: 'Technical Interview', HR_INTERVIEW: 'HR Interview', SELECTED: 'Selected', REJECTED: 'Rejected' }

/** Run an async loader and track loading / error / data. */
export function useFetch(fn, deps = []) {
  const [state, set] = useState({ data: null, loading: true, error: '' })
  const load = useCallback(() => {
    set((s) => ({ ...s, loading: true, error: '' }))
    fn().then((data) => set({ data, loading: false, error: '' }))
      .catch((e) => set({ data: null, loading: false, error: e.message }))
  }, deps) // eslint-disable-line
  useEffect(load, [load])
  return { ...state, reload: load }
}

export const Loading = () => <div className="loading"><div className="spinner" role="status" aria-label="Loading" /></div>
export const ErrorBox = ({ message, onRetry }) => message ? (
  <div className="alert err row" role="alert">{message}{onRetry && <button className="btn sm ghost right" onClick={onRetry}>Try again</button>}</div>
) : null
export const Empty = ({ title, children }) => <div className="empty"><b>{title}</b>{children}</div>
export const PageHeader = ({ title, sub, children }) => (
  <div className="page-head"><div><h1>{title}</h1>{sub && <p>{sub}</p>}</div><div className="right row wrap">{children}</div></div>
)
export const Chip = ({ children, kind = '', onRemove }) => (
  <span className={`chip ${kind}`}>{children}{onRemove && <button type="button" aria-label={`Remove ${children}`} onClick={onRemove}>×</button>}</span>
)
export const StatusPill = ({ status }) => <span className={`pill ${status}`}>{STATUS_LABEL[status] || status}</span>

export function Field({ label, error, children }) {
  return <label className="f">{label}{children}{error && <span className="field-err">{error}</span>}</label>
}

/** Circular match visualisation. Green >= 75, amber >= 40, coral below. */
export function MatchRing({ value = 0, size = 56, stroke = 6 }) {
  const r = (size - stroke) / 2, c = 2 * Math.PI * r
  const color = value >= 75 ? 'var(--mint)' : value >= 40 ? 'var(--amber)' : 'var(--coral)'
  return (
    <span className="ring" style={{ width: size, height: size, '--fs': `${Math.round(size / 3.6)}px` }} title={`${value}% skill match`}>
      <svg width={size} height={size}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#e8ecf5" strokeWidth={stroke} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={color} strokeWidth={stroke} strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c * (1 - Math.min(100, value) / 100)} />
      </svg>
      <span className="v">{Math.round(value)}%</span>
    </span>
  )
}

const STOPS = [['APPLIED', 'Applied'], ['SHORTLISTED', 'Shortlisted'], ['TECHNICAL_INTERVIEW', 'Technical'], ['HR_INTERVIEW', 'HR'], ['SELECTED', 'Selected']]
/** Application progress drawn as a route with stops. */
export function Pipeline({ status }) {
  const idx = STOPS.findIndex(([k]) => k === status)
  return (
    <div className={`path ${status === 'REJECTED' ? 'lost' : ''}`} aria-label={`Status: ${STATUS_LABEL[status]}`}>
      {STOPS.map(([k, label], i) => (
        <div key={k} className={`stop ${i < idx ? 'done' : ''} ${i === idx ? 'now done' : ''} ${k === 'SELECTED' ? 'win' : ''}`}>
          <i />{label}
        </div>
      ))}
    </div>
  )
}

export function TagInput({ value, onChange, suggestions = [], placeholder = 'Type a skill and press Enter' }) {
  const [text, setText] = useState('')
  const add = (raw) => {
    const t = raw.trim()
    if (t && !value.some((v) => v.toLowerCase() === t.toLowerCase())) onChange([...value, t])
    setText('')
  }
  const listId = 'sk-' + Math.random().toString(36).slice(2, 7)
  return (
    <div className="tagbox">
      {value.map((v) => <Chip key={v} kind="blue" onRemove={() => onChange(value.filter((x) => x !== v))}>{v}</Chip>)}
      <input list={listId} value={text} placeholder={value.length ? '' : placeholder}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); add(text) } else if (e.key === 'Backspace' && !text && value.length) onChange(value.slice(0, -1)) }}
        onBlur={() => text && add(text)} />
      <datalist id={listId}>{suggestions.filter((s) => !value.includes(s)).map((s) => <option key={s} value={s} />)}</datalist>
    </div>
  )
}

export function Modal({ title, onClose, children }) {
  useEffect(() => { const h = (e) => e.key === 'Escape' && onClose(); window.addEventListener('keydown', h); return () => window.removeEventListener('keydown', h) }, [onClose])
  return (
    <div className="modal-back" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head"><h2>{title}</h2><button className="btn sm ghost right" onClick={onClose}>Close</button></div>
        {children}
      </div>
    </div>
  )
}

export const fmtDate = (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'
export const daysLeft = (d) => d ? Math.ceil((new Date(d) - new Date().setHours(0, 0, 0, 0)) / 864e5) : null
export const money = (j) => j.salaryLpa == null ? '—' : j.jobType === 'Internship' ? `₹${Math.round(j.salaryLpa * 100000 / 12 / 1000)}k / month` : `₹${j.salaryLpa} LPA`
