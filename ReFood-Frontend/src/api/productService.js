import axiosClient from './axiosClient'

export async function getProducts() {
  const { data } = await axiosClient.get('/products/')
  return data
}