import axiosClient from './axiosClient'

export async function getProducts() {
  const { data } = await axiosClient.get('/products/')
  return data
}

export async function getProduct(id) {
  const { data } = await axiosClient.get(`/products/${id}/`)
  return data
}

export async function createProduct(formData) {
  const { data } = await axiosClient.post('/products/', formData)
  return data
}

export async function updateProduct(id, formData) {
  const { data } = await axiosClient.patch(`/products/${id}/`, formData)
  return data
}

export async function deleteProduct(id) {
  await axiosClient.delete(`/products/${id}/`)
}

export async function getCategories() {
  const { data } = await axiosClient.get('/products/categories/')
  return data
}

export async function createCategory(name) {
  const { data } = await axiosClient.post('/products/categories/', { name })
  return data
}
