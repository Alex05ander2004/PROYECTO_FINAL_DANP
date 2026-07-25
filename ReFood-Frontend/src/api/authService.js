import axiosClient from './axiosClient'

export function clearStoredAuth() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
}

export async function login(email, password) {
  const { data } = await axiosClient.post('/auth/login/', { email, password })
  return data // { access, refresh }
}

export async function getMe(accessToken) {
  const { data } = await axiosClient.get('/auth/me/', {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  return data
}

export async function validateStoredSession() {
  const token = localStorage.getItem('access_token')

  if (!token) {
    clearStoredAuth()
    return false
  }

  try {
    const user = await getMe(token)
    return user?.role === 'ADMIN'
  } catch {
    clearStoredAuth()
    return false
  }
}