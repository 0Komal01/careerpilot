// Tiny fetch wrapper: adds the JWT, parses JSON, and turns API errors into readable Error objects.
const BASE = import.meta.env.VITE_API_URL || '/api'
export const tokenStore = {
  get: () => localStorage.getItem('cp_token'),
  set: (t) => localStorage.setItem('cp_token', t),
  clear: () => { localStorage.removeItem('cp_token'); localStorage.removeItem('cp_user') },
}

async function request(path, { method = 'GET', body, form } = {}) {
  const headers = {}
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body) headers['Content-Type'] = 'application/json'
  let res
  try {
    res = await fetch(BASE + path, { method, headers, body: form || (body ? JSON.stringify(body) : undefined) })
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running on port 8080.')
  }
  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    if (res.status === 401 && token && !path.startsWith('/auth')) {
      tokenStore.clear()
      window.location.href = '/login'
    }
    const err = new Error(data?.message || `Request failed (${res.status})`)
    err.fieldErrors = data?.fieldErrors || {}
    err.status = res.status
    throw err
  }
  return data
}

export const api = {
  get: (p) => request(p),
  post: (p, body) => request(p, { method: 'POST', body }),
  put: (p, body) => request(p, { method: 'PUT', body }),
  del: (p) => request(p, { method: 'DELETE' }),
  upload: (p, form) => request(p, { method: 'POST', form }),
}
