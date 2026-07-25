import { useEffect, useMemo, useState } from 'react'
import { ChevronDown, Clock, CheckCircle2, Truck, XCircle, Hourglass, Eye } from 'lucide-react'
import { getOrders, updateOrderStatus } from '../api/orderService'
import OrderDetailModal from '../components/OrderDetailModal'
import AdminHeader from '../components/AdminHeader'
import TableSkeleton from '../components/TableSkeleton'
import SearchFilterBar from '../components/SearchFilterBar'

const STATUS_OPTIONS = [
  { value: 'PENDIENTE', label: 'Pendiente', color: 'var(--color-status-pending)', icon: Clock },
  { value: 'EN_PREPARACION', label: 'En preparación', color: 'var(--color-status-preparing)', icon: Hourglass },
  { value: 'LISTO', label: 'Listo para entrega', color: 'var(--color-status-ready)', icon: CheckCircle2 },
  { value: 'ENTREGADO', label: 'Entregado', color: 'var(--color-status-delivered)', icon: Truck },
  { value: 'CANCELADO', label: 'Cancelado', color: 'var(--color-status-cancelled)', icon: XCircle },
]

const PAYMENT_LABELS = {
  TARJETA: 'Tarjeta',
  YAPE: 'Yape',
  PLIN: 'Plin',
}

const ORDER_FILTERS = [
  {
    key: 'status',
    label: 'Estado',
    options: STATUS_OPTIONS.map((s) => ({ value: s.value, label: s.label })),
  },
  {
    key: 'payment',
    label: 'Tipo de pago',
    options: Object.entries(PAYMENT_LABELS).map(([value, label]) => ({ value, label })),
  },
]

const EMPTY_FILTERS = { status: null, payment: null }

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
  const [search, setSearch] = useState('')
  const [activeFilters, setActiveFilters] = useState(EMPTY_FILTERS)
  const [selectedOrder, setSelectedOrder] = useState(null)

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

  const filtered = useMemo(() => {
    return orders.filter((o) => {
      if (search && !o.user_name?.toLowerCase().includes(search.toLowerCase())) return false
      if (activeFilters.status && o.status !== activeFilters.status) return false
      if (activeFilters.payment && o.payment_method !== activeFilters.payment) return false
      return true
    })
  }, [orders, search, activeFilters])

  function handleFilterChange(key, value) {
    setActiveFilters((prev) => ({ ...prev, [key]: value }))
  }

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

        <SearchFilterBar
          search={search}
          onSearchChange={setSearch}
          searchPlaceholder="Buscar por nombre de cliente..."
          filters={ORDER_FILTERS}
          activeFilters={activeFilters}
          onFilterChange={handleFilterChange}
        />

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
                  <th className="px-4 py-3 font-medium">Detalle</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((order) => {
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
                          <div className="text-xs text-ink-soft">
                            {order.payment_method === 'TARJETA'
                              ? `Tarjeta •••• ${order.payment_reference}`
                              : `Op: ${order.payment_reference}`}
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          {meta.icon && (
                            <meta.icon
                              className="w-4 h-4 shrink-0"
                              style={{ color: meta.color }}
                            />
                          )}
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
                      <td className="px-4 py-3 text-right">
                        <button
                          onClick={() => setSelectedOrder(order)}
                          className="inline-flex items-center gap-1.5 rounded-sm border border-line px-2.5 py-1.5 text-xs font-medium text-ink transition hover:bg-surface-sunken"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          Ver detalle
                        </button>
                      </td>
                    </tr>
                  )
                })}

                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-4 py-6 text-center text-ink-soft">
                      {orders.length === 0
                        ? 'No hay pedidos registrados.'
                        : 'Ningún pedido coincide con la búsqueda.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            <OrderDetailModal
              order={selectedOrder}
              statusMeta={statusMeta}
              onClose={() => setSelectedOrder(null)}
            />
          </div>
        )}
      </main>
    </div>
  )
}
