import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Layout from './components/Layout.jsx'
import { Login, Register } from './pages/Auth.jsx'
import Dashboard from './pages/student/Dashboard.jsx'
import Profile from './pages/student/Profile.jsx'
import Jobs from './pages/student/Jobs.jsx'
import JobDetail from './pages/student/JobDetail.jsx'
import Applications from './pages/student/Applications.jsx'
import { ResumeAnalyzer, JdAnalyzer, Interview } from './pages/student/AiTools.jsx'
import { AdminDashboard, Companies, JobManagement, AdminApplications } from './pages/admin/Admin.jsx'

function Guard({ role, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (user.role !== role) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} replace />
  return children
}

export default function App() {
  const { user } = useAuth()
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/'} replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />
      <Route element={<Guard role="STUDENT"><Layout /></Guard>}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/jobs" element={<Jobs />} />
        <Route path="/jobs/:id" element={<JobDetail />} />
        <Route path="/applications" element={<Applications />} />
        <Route path="/resume" element={<ResumeAnalyzer />} />
        <Route path="/jd" element={<JdAnalyzer />} />
        <Route path="/interview" element={<Interview />} />
      </Route>
      <Route element={<Guard role="ADMIN"><Layout /></Guard>}>
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/companies" element={<Companies />} />
        <Route path="/admin/jobs" element={<JobManagement />} />
        <Route path="/admin/applications" element={<AdminApplications />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
