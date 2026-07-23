import { useEffect, useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { getOrders, updateOrderStatus } from '../api/orderService'
import AdminHeader from '../components/AdminHeader'
import TableSkeleton from '../components/TableSkeleton'

const STATUS_OPTIONS = [
  { value: 'PENDIENTE', label: 'Pendiente', color: 'var(--color-status-pending)' },
  { value: 'EN_PREPARACION', label: 'En preparación', color: 'var(--color-status-preparing)' },
  { value: 'LISTO', label: 'Listo para entrega', color: 'var(--color-status-ready)' },
  { value: 'ENTREGADO', label: 'Entregado', color: 'var(--color-status-delivered)' },
  { value: 'CANCELADO', label: 'Cancelado', color: 'var(--color-status-cancelled)' },
]

const PAYMENT_LABELS = {
  EFECTIVO: 'Efectivo',
  YAPE: 'Yape',
  PLIN: 'Plin',
}

function statusMeta(status) {
  return STATUS_OPTIONS.find((option) => option.value === status) ?? STATUS_OPTIONS[0]
}

function formatDate(isoString) {
  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return isoString
  return date.toLocaleString('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function itemsSummary(items) {
  if (!items?.length) return '—'
  return items.map((item) => `${item.quantity}x ${item.product_name}`).join(', ')
}

export default function OrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)

  useEffect(() => {
    async function fetchOrders() {
      try {
        const data = await getOrders()
        setOrders(data)
      } catch {
        setError('No se pudieron cargar los pedidos.')
      } finally {
        setLoading(false)
      }
    }
    fetchOrders()
  }, [])

  async function handleStatusChange(orderId, newStatus) {
    const previous = orders
    setOrders((current) =>
      current.map((order) => (order.id === orderId ? { ...order, status: newStatus } : order))
    )
    setUpdatingId(orderId)
    try {
      await updateOrderStatus(orderId, newStatus)
    } catch {
      setOrders(previous)
      setError('No se pudo actualizar el estado del pedido.')
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AdminHeader />

      <main className="max-w-6xl mx-auto px-6 py-8">
        <h1 className="text-xl font-semibold text-ink tracking-tight mb-6">Pedidos</h1>

        {loading && <TableSkeleton columns={6} />}

        {error && (
          <p className="text-sm text-error bg-error-soft border border-error/30 rounded-sm px-3 py-2 mb-4">
            {error}
          </p>
        )}

        {!loading && (
          <div className="bg-surface border border-line rounded-md overflow-hidden">
            <table className="w-full text-sm text-left">
              <thead className="text-ink-soft text-xs uppercase tracking-wide">
                <tr>
                  <th className="px-4 py-3 font-medium">Pedido</th>
                  <th className="px-4 py-3 font-medium">Cliente</th>
                  <th className="px-4 py-3 font-medium">Productos</th>
                  <th className="px-4 py-3 font-medium">Total</th>
                  <th className="px-4 py-3 font-medium">Pago</th>
                  <th className="px-4 py-3 font-medium">Estado</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => {
                  const meta = statusMeta(order.status)
                  return (
                    <tr key={order.id} className="border-t border-line hover:bg-surface-sunken/60">
                      <td className="px-4 py-3">
                        <div className="font-medium text-ink">Pedido #{order.id}</div>
                        <div className="text-xs text-ink-soft">{formatDate(order.created_at)}</div>
                      </td>
                      <td className="px-4 py-3">
                        <div className="text-ink">{order.user_name}</div>
                        <div className="text-xs text-ink-soft">{order.user_email}</div>
                      </td>
                      <td className="px-4 py-3 text-ink-soft max-w-xs">{itemsSummary(order.items)}</td>
                      <td className="px-4 py-3 text-ink font-medium">S/ {order.total}</td>
                      <td className="px-4 py-3">
                        <div className="text-ink-soft">
                          {PAYMENT_LABELS[order.payment_method] ?? order.payment_method}
                        </div>
                        {order.payment_reference && (
                          <div className="text-xs text-ink-soft">Op: {order.payment_reference}</div>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <span
                            className="w-1.5 h-1.5 rounded-full shrink-0"
                            style={{ backgroundColor: meta.color }}
                          />
                          <div className="relative">
                            <select
                              value={order.status}
                              disabled={updatingId === order.id}
                              onChange={(e) => handleStatusChange(order.id, e.target.value)}
                              className="appearance-none bg-transparent border border-line rounded-sm text-xs text-ink pl-2 pr-7 py-1.5 focus:outline-none focus:border-accent disabled:opacity-60"
                            >
                              {STATUS_OPTIONS.map((option) => (
                                <option
                                  key={option.value}
                                  value={option.value}
                                  style={{ backgroundColor: 'var(--color-surface)', color: 'var(--color-ink)' }}
                                >
                                  {option.label}
                                </option>
                              ))}
                            </select>
                            <ChevronDown className="w-3.5 h-3.5 text-ink-soft absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none" />
                          </div>
                        </div>
                      </td>
                    </tr>
                  )
                })}

                {orders.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-6 text-center text-ink-soft">
                      No hay pedidos registrados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  )
}
