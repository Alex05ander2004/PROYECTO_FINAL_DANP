import axiosClient from './axiosClient'

export async function getUsers() {
  const { data } = await axiosClient.get('/auth/users/')
  return data
}

export async function updateUser(id, fields) {
  const { data } = await axiosClient.patch(`/auth/users/${id}/`, fields)
  return data
}
