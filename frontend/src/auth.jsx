import { createContext, useContext, useState } from 'react'
import { api, tokenStore } from './api.js'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('cp_user')) } catch { return null }
  })

  const finish = (res) => {
    tokenStore.set(res.token)
    const u = { email: res.email, name: res.name, role: res.role }
    localStorage.setItem('cp_user', JSON.stringify(u))
    setUser(u)
    return u
  }
  const login = async (email, password) => finish(await api.post('/auth/login', { email, password }))
  const register = async (name, email, password) => finish(await api.post('/auth/register', { name, email, password }))
  const logout = () => { tokenStore.clear(); setUser(null) }

  return <Ctx.Provider value={{ user, login, register, logout, setUser }}>{children}</Ctx.Provider>
}
