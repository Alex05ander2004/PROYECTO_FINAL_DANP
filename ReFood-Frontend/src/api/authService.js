import axiosClient from './axiosClient'

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