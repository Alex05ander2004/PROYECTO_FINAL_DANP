import { useEffect, useMemo, useRef, useState } from 'react'
import { AlertTriangle, Boxes, Download, Package, ShoppingBag, Users } from 'lucide-react'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas-pro'
import AdminHeader from '../components/AdminHeader'
import { getOrders } from '../api/orderService'
import { getProducts } from '../api/productService'
import { getUsers } from '../api/userService'
import { useTheme } from '../context/ThemeContext'

const STATUS_META = {
  PENDIENTE: { label: 'Pendiente', color: '#ad6a2e' },
  EN_PREPARACION: { label: 'En preparación', color: '#5b6bc0' },
  LISTO: { label: 'Listo', color: '#16a34a' },
  ENTREGADO: { label: 'Entregado', color: '#3d6b3d' },
  CANCELADO: { label: 'Cancelado', color: '#b3261e' },
}

const EXPIRY_THRESHOLD_DAYS = 7
const LOW_STOCK_THRESHOLD = 5
const DELIVERED_STATUS = 'ENTREGADO'

function formatCurrency(value) {
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: 'PEN',
    maximumFractionDigits: 0,
  }).format(value)
}

function toDateKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatDateKey(dateKey) {
  const [year, month, day] = dateKey.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return date.toLocaleDateString('es-PE', {
    day: '2-digit',
    month: 'short',
  })
}

function parseDate(value) {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function formatFileDate(date) {
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}-${month}-${year}`
}

export default function DashboardPage() {
  const [orders, setOrders] = useState([])
  const [users, setUsers] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [exporting, setExporting] = useState(false)
  const dashboardRef = useRef(null)
  const { theme } = useTheme()

  useEffect(() => {
    async function loadDashboard() {
      try {
        const [ordersData, usersData, productsData] = await Promise.all([
          getOrders(),
          getUsers(),
          getProducts(),
        ])

        setOrders(ordersData || [])
        setUsers(usersData || [])
        setProducts(productsData || [])
      } catch {
        setError('No se pudieron cargar los datos del dashboard.')
      } finally {
        setLoading(false)
      }
    }

    loadDashboard()
  }, [])

  const metrics = useMemo(() => {
    const today = new Date()
    const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate())
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - 6)

    const deliveredOrders = orders.filter((order) => String(order.status || '').toUpperCase() === DELIVERED_STATUS)
    const todayOrders = deliveredOrders.filter((order) => {
      const createdAt = parseDate(order.created_at)
      return createdAt && createdAt >= startOfToday
    })
    const weekOrders = deliveredOrders.filter((order) => {
      const createdAt = parseDate(order.created_at)
      return createdAt && createdAt >= startOfWeek
    })

    const expiredSoon = products.filter((product) => {
      const expiry = parseDate(`${product.expiration_date}T00:00:00`)
      if (!expiry) return false
      const diffDays = Math.round((expiry - today) / 86_400_000)
      return diffDays >= 0 && diffDays <= EXPIRY_THRESHOLD_DAYS
    })

    const lowStock = products.filter((product) => Number(product.stock || 0) < LOW_STOCK_THRESHOLD)

    const salesByDay = Array.from({ length: 7 }, (_, index) => {
      const date = new Date(today)
      date.setDate(today.getDate() - (6 - index))
      const key = toDateKey(date)
      const total = deliveredOrders.reduce((sum, order) => {
        const createdAt = parseDate(order.created_at)
        if (!createdAt) return sum
        const orderKey = toDateKey(createdAt)
        return orderKey === key ? sum + Number(order.total || 0) : sum
      }, 0)

      return { key, label: formatDateKey(key), value: total }
    })

    const statusCounts = Object.keys(STATUS_META).reduce((acc, key) => {
      acc[key] = orders.filter((order) => String(order.status || '').toUpperCase() === key).length
      return acc
    }, {})

    const topProducts = deliveredOrders
      .flatMap((order) => order.items || [])
      .reduce((acc, item) => {
        const key = item.product_name || 'Producto'
        const existing = acc.find((entry) => entry.name === key)
        if (existing) {
          existing.quantity += Number(item.quantity || 0)
        } else {
          acc.push({ name: key, quantity: Number(item.quantity || 0) })
        }
        return acc
      }, [])
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 6)

    return {
      todayOrders: todayOrders.length,
      weekOrders: weekOrders.length,
      todayRevenue: todayOrders.reduce((sum, order) => sum + Number(order.total || 0), 0),
      weekRevenue: weekOrders.reduce((sum, order) => sum + Number(order.total || 0), 0),
      totalUsers: users.length,
      expiringProducts: expiredSoon.length,
      lowStockProducts: lowStock.length,
      salesByDay,
      statusCounts,
      topProducts,
    }
  }, [orders, users, products])

  const totalStatus = Object.values(metrics.statusCounts).reduce((sum, value) => sum + value, 0)
  const donutSegments = Object.entries(STATUS_META).reduce((segments, [key, meta], index) => {
    const value = metrics.statusCounts[key]
    if (!value) return segments

    const start = Object.entries(STATUS_META)
      .slice(0, index)
      .reduce((sum, [statusKey]) => sum + (metrics.statusCounts[statusKey] || 0), 0)
    const end = start + value
    const startPercent = (start / Math.max(totalStatus, 1)) * 100
    const endPercent = (end / Math.max(totalStatus, 1)) * 100

    segments.push(`${meta.color} ${startPercent}% ${endPercent}%`)
    return segments
  }, [])
  
  // Generación de imagen y PDF del dashboard
  async function captureDashboard() {
    if (!dashboardRef.current) return null

    return html2canvas(dashboardRef.current, {
      scale: 2,
      useCORS: true,
      backgroundColor: getComputedStyle(document.body).backgroundColor || '#ffffff',
    })
  }

  async function handleExportImage() {
    setExporting(true)

    try {
      const canvas = await captureDashboard()
      if (!canvas) return

      const link = document.createElement('a')
      link.download = `dashboard-refood-${formatFileDate(new Date())}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    } catch (error) {
      console.error('No se pudo generar la imagen.', error)
      window.alert(`No se pudo generar la imagen: ${error.message || 'error desconocido'}`)
    } finally {
      setExporting(false)
    }
  }

  async function handleExportPdf() {
    setExporting(true)

    try {
      const canvas = await captureDashboard()
      if (!canvas) return

      const imgData = canvas.toDataURL('image/png')

      const doc = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' })
      const pageWidth = doc.internal.pageSize.getWidth()
      const pageHeight = doc.internal.pageSize.getHeight()

      const imgWidth = pageWidth
      const imgHeight = (canvas.height * imgWidth) / canvas.width

      let heightLeft = imgHeight
      let position = 0

      doc.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight

      while (heightLeft > 0) {
        position = heightLeft - imgHeight
        doc.addPage()
        doc.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
        heightLeft -= pageHeight
      }

      doc.save(`dashboard-refood-${formatFileDate(new Date())}.pdf`)
    } catch (error) {
      console.error('No se pudo generar el PDF.', error)
      window.alert(`No se pudo generar el PDF: ${error.message || 'error desconocido'}`)
    } finally {
      setExporting(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-paper">
        <AdminHeader />
        <main className="max-w-7xl mx-auto px-6 py-8">
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
            {Array.from({ length: 5 }).map((_, index) => (
              <div key={index} className="h-32 rounded-md border border-line bg-surface animate-pulse" />
            ))}
          </div>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-paper">
      <AdminHeader />

      <main className="max-w-7xl mx-auto px-6 py-8 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-medium uppercase tracking-[0.2em] text-accent">Dashboard</p>
            <h1 className="text-xl font-semibold text-ink tracking-tight">Resumen del negocio</h1>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleExportImage}
              disabled={exporting}
              className="inline-flex items-center gap-2 rounded-sm border border-line bg-surface px-3 py-2 text-sm font-medium text-ink transition hover:bg-surface-sunken disabled:opacity-60"
            >
              <Download className="w-4 h-4" />
              {exporting ? 'Generando...' : 'Descargar imagen'}
            </button>

            <button
              onClick={handleExportPdf}
              disabled={exporting}
              className="inline-flex items-center gap-2 rounded-sm border border-line bg-surface px-3 py-2 text-sm font-medium text-ink transition hover:bg-surface-sunken disabled:opacity-60"
            >
              <Download className="w-4 h-4" />
              {exporting ? 'Generando PDF...' : 'Descargar PDF'}
            </button>
          </div>
        </div>

        {error ? (
          <div className="rounded-md border border-error/30 bg-error-soft px-4 py-3 text-sm text-error">
            {error}
          </div>
        ) : (
          <>
            <div ref={dashboardRef} className="space-y-6">
              <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
              <MetricCard title="Pedidos hoy" value={metrics.todayOrders} icon={<ShoppingBag className="w-5 h-5" />} accent="accent" />
              <MetricCard title="Pedidos esta semana" value={metrics.weekOrders} icon={<Package className="w-5 h-5" />} accent="deal" />
              <MetricCard title="Usuarios registrados y activos" value={metrics.totalUsers} icon={<Users className="w-5 h-5" />} accent="accent" />
              <MetricCard title="Próx. a vencer (7 días)" value={metrics.expiringProducts} icon={<AlertTriangle className="w-5 h-5" />} accent="error" />
              <MetricCard title="Stock bajo (< 5)" value={metrics.lowStockProducts} icon={<Boxes className="w-5 h-5" />} accent="deal" />
            </section>

              <section className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
              <div className="rounded-md border border-line bg-surface p-5">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-ink">Pedidos por estado</h2>
                </div>

                <div className="flex flex-col gap-6 md:flex-row md:items-center">
                  <div
                    className="relative mx-auto h-48 w-48 rounded-full"
                    style={{ background: `conic-gradient(${donutSegments.join(', ')})` }}
                  >
                    <div className="absolute inset-6 rounded-full bg-surface" />
                    <div className="absolute inset-0 flex items-center justify-center flex-col text-center">
                      <span className="text-2xl font-semibold text-ink">{totalStatus}</span>
                      <span className="text-sm text-ink-soft">pedidos</span>
                    </div>
                  </div>

                  <div className="flex-1 space-y-3">
                    {Object.entries(STATUS_META).map(([key, meta]) => {
                      const count = metrics.statusCounts[key]
                      return (
                        <div key={key} className="flex items-center justify-between rounded-sm border border-line px-3 py-2">
                          <div className="flex items-center gap-2">
                            <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: meta.color }} />
                            <span className="text-sm text-ink">{meta.label}</span>
                          </div>
                          <span className="text-sm font-medium text-ink">{count}</span>
                        </div>
                      )
                    })}
                  </div>
                </div>
              </div>

              <div className="rounded-md border border-line bg-surface p-5">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-ink">Ventas por día</h2>
                  <span className="text-sm text-ink-soft">Últimos 7 días</span>
                </div>

                <div className="flex h-56 items-end gap-3 mt-6">
                  {metrics.salesByDay.map((day) => {
                    const maxValue = Math.max(...metrics.salesByDay.map((entry) => entry.value), 1)
                    const barHeight = day.value === 0 ? 8 : Math.max(18, (day.value / maxValue) * 100)

                    return (
                      <div key={day.key} className="flex-1 h-full flex flex-col justify-end items-center gap-2">
                        <div className="flex h-full w-full items-end justify-center">
                          <div
                            className="w-full max-w-8 rounded-t-sm bg-accent/80"
                            style={{ height: `${barHeight}%`, minHeight: day.value === 0 ? '8px' : '18px' }}
                          />
                        </div>
                        <div className="text-center">
                          <div className="text-xs font-medium text-ink">{day.label}</div>
                          <div className="text-[11px] text-ink-soft">{formatCurrency(day.value)}</div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </section>

              <section className="rounded-md border border-line bg-surface p-5">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-ink">Top productos más vendidos</h2>
                <span className="text-sm text-ink-soft">Por cantidad</span>
              </div>

              <div className="space-y-3">
                {metrics.topProducts.length > 0 ? (
                  metrics.topProducts.map((product, index) => {
                    const max = Math.max(...metrics.topProducts.map((item) => item.quantity), 1)
                    return (
                      <div key={`${product.name}-${index}`}>
                        <div className="mb-1 flex items-center justify-between text-sm">
                          <span className="text-ink">{product.name}</span>
                          <span className="text-ink-soft">{product.quantity} und</span>
                        </div>
                        <div className="h-2 rounded-full bg-surface-sunken">
                          <div className="h-2 rounded-full bg-accent" style={{ width: `${(product.quantity / max) * 100}%` }} />
                        </div>
                      </div>
                    )
                  })
                ) : (
                  <p className="text-sm text-ink-soft">Todavía no hay ventas registradas.</p>
                )}
              </div>
              </section>
            </div>
          </>
        )}
      </main>
    </div>
  )
}

function MetricCard({ title, value, subtitle, icon, accent }) {
  const accentClasses = {
    accent: 'bg-accent/10 text-accent',
    deal: 'bg-deal-soft text-deal',
    error: 'bg-error-soft text-error',
  }

  return (
    <div className="rounded-md border border-line bg-surface p-4">
      <div className={`inline-flex rounded-sm p-2 ${accentClasses[accent]}`}>{icon}</div>
      <p className="mt-4 text-sm text-ink-soft">{title}</p>
      <p className="mt-1 text-2xl font-semibold text-ink">{value}</p>
      <p className="mt-1 text-sm text-ink-soft">{subtitle}</p>
    </div>
  )
}
