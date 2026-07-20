import { useEffect, useState } from 'react'
import { Pencil, Trash2, Plus, Loader2 } from 'lucide-react'
import { getProducts } from '../api/productService'

export default function ProductsPage() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Productos</h1>
          <button className="flex items-center gap-2 bg-green-700 hover:bg-green-800 text-white px-4 py-2 rounded-lg transition">
            <Plus className="w-4 h-4" />
            Nuevo producto
          </button>
        </div>

        {loading && (
          <div className="flex items-center gap-2 text-gray-500">
            <Loader2 className="w-4 h-4 animate-spin" />
            Cargando productos...
          </div>
        )}

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        {!loading && !error && (
          <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            <table className="w-full text-sm text-left">
              <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                <tr>
                  <th className="px-4 py-3">Imagen</th>
                  <th className="px-4 py-3">Nombre</th>
                  <th className="px-4 py-3">Categoría</th>
                  <th className="px-4 py-3">Precio</th>
                  <th className="px-4 py-3">Stock</th>
                  <th className="px-4 py-3">Vence</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id} className="border-t border-gray-100 hover:bg-gray-50">
                    <td className="px-4 py-3">
                      {product.image ? (
                        <img
                          src={product.image}
                          alt={product.name}
                          className="w-12 h-12 object-cover rounded-lg"
                        />
                      ) : (
                        <div className="w-12 h-12 bg-gray-200 rounded-lg" />
                      )}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{product.name}</td>
                    <td className="px-4 py-3 text-gray-500">{product.category}</td>
                    <td className="px-4 py-3">
                      {product.discount_price ? (
                        <div>
                          <span className="line-through text-gray-400 text-xs mr-1">
                            S/ {product.price}
                          </span>
                          <span className="text-green-700 font-medium">
                            S/ {product.discount_price}
                          </span>
                        </div>
                      ) : (
                        <span className="text-gray-700">S/ {product.price}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-gray-700">
                      {product.stock} {product.unit}
                    </td>
                    <td className="px-4 py-3 text-gray-500">{product.expiration_date}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`text-xs font-medium px-2 py-1 rounded-full ${
                          product.is_active
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-200 text-gray-500'
                        }`}
                      >
                        {product.is_active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <button className="p-2 rounded-lg hover:bg-gray-100 text-gray-500">
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button className="p-2 rounded-lg hover:bg-red-50 text-red-500">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {products.length === 0 && (
                  <tr>
                    <td colSpan={8} className="px-4 py-6 text-center text-gray-400">
                      No hay productos registrados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}