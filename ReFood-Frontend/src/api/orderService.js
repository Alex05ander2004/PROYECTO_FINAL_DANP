import axiosClient from './axiosClient'

export async function getOrders() {
  const { data } = await axiosClient.get('/orders/')
  return data
}

export async function updateOrderStatus(orderId, status) {
  const { data } = await axiosClient.patch(`/orders/${orderId}/`, { status })
  return data
}
