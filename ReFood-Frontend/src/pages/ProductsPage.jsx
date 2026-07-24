import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Pencil, Trash2, Plus } from 'lucide-react'
import { deleteProduct, getProducts } from '../api/productService'
import AdminHeader from '../components/AdminHeader'
import TableSkeleton from '../components/TableSkeleton'
import SearchFilterBar from '../components/SearchFilterBar'

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

const PRODUCT_FILTERS = [
  {
    key: 'category',
    label: 'Categoría',
    options: [],
  },
  {
    key: 'isOffer',
    label: 'Tipo',
    options: [
      { value: 'yes', label: 'Es oferta' },
      { value: 'no', label: 'Sin oferta' },
    ],
  },
  {
    key: 'expiry',
    label: 'Vencimiento',
    options: [
      { value: 'expired', label: 'Vencidos' },
      { value: '2', label: 'Próx. 2 días' },
      { value: '7', label: 'Próx. 7 días' },
      { value: '30', label: 'Próx. 30 días' },
      { value: 'ok', label: 'Sin vencer' },
    ],
  },
  {
    key: 'active',
    label: 'Estado',
    options: [
      { value: 'yes', label: 'Solo activos' },
      { value: 'no', label: 'Solo inactivos' },
    ],
  },
  {
    key: 'stock',
    label: 'Stock',
    options: [
      { value: 'yes', label: 'Con stock' },
      { value: 'no', label: 'Sin stock' },
    ],
  },
]

const EMPTY_FILTERS = {
  category: null,
  isOffer: null,
  expiry: null,
  active: null,
  stock: null,
}

export default function ProductsPage() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [activeFilters, setActiveFilters] = useState(EMPTY_FILTERS)
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

  // Construcción de categorías
  const filtersWithCategories = useMemo(() => {
    const categories = [...new Set(products.map((p) => p.category).filter(Boolean))].sort()
    return PRODUCT_FILTERS.map((f) =>
      f.key === 'category'
        ? { ...f, options: categories.map((c) => ({ value: c, label: c })) }
        : f
    )
  }, [products])

  const filtered = useMemo(() => {
    return products.filter((p) => {
      if (search && !p.name.toLowerCase().includes(search.toLowerCase())) return false
      if (activeFilters.category && p.category !== activeFilters.category) return false
      if (activeFilters.isOffer === 'yes' && !p.is_featured_offer) return false
      if (activeFilters.isOffer === 'no' && p.is_featured_offer) return false
      if (activeFilters.expiry) {
        const days = daysUntil(p.expiration_date)
        if (days === null) return false
        if (activeFilters.expiry === 'expired' && days >= 0) return false
        if (activeFilters.expiry === '2' && (days < 0 || days > 2)) return false
        if (activeFilters.expiry === '7' && (days < 0 || days > 7)) return false
        if (activeFilters.expiry === '30' && (days < 0 || days > 30)) return false
        if (activeFilters.expiry === 'ok' && days < 0) return false
      }
      if (activeFilters.active === 'yes' && !p.is_active) return false
      if (activeFilters.active === 'no' && p.is_active) return false
      if (activeFilters.stock === 'yes' && p.stock <= 0) return false
      if (activeFilters.stock === 'no' && p.stock > 0) return false

      return true
    })
  }, [products, search, activeFilters])

  function handleFilterChange(key, value) {
    setActiveFilters((prev) => ({ ...prev, [key]: value }))
  }

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

        <SearchFilterBar
          search={search}
          onSearchChange={setSearch}
          searchPlaceholder="Buscar por nombre..."
          filters={filtersWithCategories}
          activeFilters={activeFilters}
          onFilterChange={handleFilterChange}
        />

        {loading && <TableSkeleton columns={8} />}

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
                {filtered.map((product) => (
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
                      {product.discount_price != null ? (
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
                          className={`w-1.5 h-1.5 rounded-full ${product.is_active ? 'bg-accent' : 'bg-ink-soft'
                            }`}
                        />
                        {product.is_active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <button
                          onClick={() => navigate(`/products/${product.id}/edit`)}
                          className="p-2 rounded-sm text-blue-600 hover:bg-blue-600 hover:text-white transition-colors"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(product)}
                          className="p-2 rounded-sm text-red-600 hover:bg-red-600 hover:text-white transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={8} className="px-4 py-6 text-center text-ink-soft">
                      {products.length === 0
                        ? 'No hay productos registrados.'
                        : 'Ningún producto coincide con la búsqueda.'}
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
