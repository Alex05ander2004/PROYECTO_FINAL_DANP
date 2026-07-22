import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Pencil, Trash2, Plus, Loader2 } from 'lucide-react'
import { deleteProduct, getProducts } from '../api/productService'
import AdminHeader from '../components/AdminHeader'

function daysUntil(dateString) {
  const target = new Date(`${dateString}T00:00:00`)
  if (Number.isNaN(target.getTime())) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((target - today) / 86_400_000)
}

function ExpiryLabel({ dateString }) {
  const days = daysUntil(dateString)
  if (days === null) return <span className="text-ink-soft">{dateString}</span>
  if (days < 0) return <span className="text-error font-medium">Vencido</span>
  if (days === 0) return <span className="text-deal font-medium">Vence hoy</span>
  if (days <= 2) return <span className="text-deal font-medium">Vence en {days}d</span>
  return <span className="text-ink-soft">{dateString}</span>
}

export default function ProductsPage() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    async function fetchProducts() {
      try {
        const data = await getProducts()
        setProducts(data)
      } catch {
        setError('No se pudieron cargar los productos.')
      } finally {
        setLoading(false)
      }
    }
    fetchProducts()
  }, [])

  async function handleDelete(product) {
    if (!window.confirm(`¿Eliminar "${product.name}"? Esta acción no se puede deshacer.`)) {
      return
    }
    try {
      await deleteProduct(product.id)
      setProducts((current) => current.filter((p) => p.id !== product.id))
    } catch {
      setError('No se pudo eliminar el producto.')
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AdminHeader />

      <main className="max-w-6xl mx-auto px-6 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-xl font-semibold text-ink tracking-tight">Productos</h1>
          <button
            onClick={() => navigate('/products/new')}
            className="flex items-center gap-2 bg-accent hover:opacity-90 text-on-accent text-sm font-medium px-4 py-2 rounded-sm transition"
          >
            <Plus className="w-4 h-4" />
            Nuevo producto
          </button>
        </div>

        {loading && (
          <div className="flex items-center gap-2 text-ink-soft text-sm">
            <Loader2 className="w-4 h-4 animate-spin" />
            Cargando productos...
          </div>
        )}

        {error && (
          <p className="text-sm text-error bg-error-soft border border-error/30 rounded-sm px-3 py-2">
            {error}
          </p>
        )}

        {!loading && !error && (
          <div className="bg-surface border border-line rounded-md overflow-hidden">
            <table className="w-full text-sm text-left">
              <thead className="text-ink-soft text-xs uppercase tracking-wide">
                <tr>
                  <th className="px-4 py-3 font-medium">Imagen</th>
                  <th className="px-4 py-3 font-medium">Nombre</th>
                  <th className="px-4 py-3 font-medium">Categoría</th>
                  <th className="px-4 py-3 font-medium">Precio</th>
                  <th className="px-4 py-3 font-medium">Stock</th>
                  <th className="px-4 py-3 font-medium">Vence</th>
                  <th className="px-4 py-3 font-medium">Estado</th>
                  <th className="px-4 py-3 font-medium text-right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id} className="border-t border-line hover:bg-surface-sunken/60">
                    <td className="px-4 py-3">
                      {product.image ? (
                        <img
                          src={product.image}
                          alt={product.name}
                          className="w-12 h-12 object-cover rounded-sm border border-line"
                        />
                      ) : (
                        <div className="w-12 h-12 bg-surface-sunken rounded-sm border border-line" />
                      )}
                    </td>
                    <td className="px-4 py-3 font-medium text-ink">{product.name}</td>
                    <td className="px-4 py-3 text-ink-soft">{product.category}</td>
                    <td className="px-4 py-3">
                      {product.discount_price ? (
                        <div>
                          <span className="line-through text-ink-soft text-xs mr-1">
                            S/ {product.price}
                          </span>
                          <span className="text-deal font-medium">
                            S/ {product.discount_price}
                          </span>
                        </div>
                      ) : (
                        <span className="text-ink">S/ {product.price}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-ink-soft">
                      {product.stock} {product.unit}
                    </td>
                    <td className="px-4 py-3">
                      <ExpiryLabel dateString={product.expiration_date} />
                    </td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center gap-1.5 text-xs font-medium text-ink">
                        <span
                          className={`w-1.5 h-1.5 rounded-full ${
                            product.is_active ? 'bg-accent' : 'bg-ink-soft'
                          }`}
                        />
                        {product.is_active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <button
                          onClick={() => navigate(`/products/${product.id}/edit`)}
                          className="p-2 rounded-sm hover:bg-surface-sunken text-ink-soft"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(product)}
                          className="p-2 rounded-sm hover:bg-error-soft text-error"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {products.length === 0 && (
                  <tr>
                    <td colSpan={8} className="px-4 py-6 text-center text-ink-soft">
                      No hay productos registrados.
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
