import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import {
  createCategory,
  createProduct,
  getCategories,
  getProduct,
  updateProduct,
} from '../api/productService'
import AdminHeader from '../components/AdminHeader'

const EMPTY_FORM = {
  name: '',
  description: '',
  category: '',
  price: '',
  discount_percentage: '',
  unit: '',
  stock: '',
  expiration_date: '',
  is_featured_offer: false,
  is_active: true,
}

const inputClass =
  'w-full px-3 py-2 bg-transparent border border-line rounded-sm text-sm text-ink placeholder:text-ink-soft/70 focus:outline-none focus:border-accent'
const labelClass = 'block text-xs font-medium text-ink-soft mb-1'

export default function ProductFormPage() {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState(EMPTY_FORM)
  const [categories, setCategories] = useState([])
  const [isNewCategory, setIsNewCategory] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState('')
  const [imageFile, setImageFile] = useState(null)
  const [currentImageUrl, setCurrentImageUrl] = useState('')
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadInitialData() {
      try {
        const categoryList = await getCategories()
        setCategories(categoryList)

        if (isEditing) {
          const product = await getProduct(id)
          setForm({
            name: product.name,
            description: product.description,
            category: product.category,
            price: product.price,
            discount_percentage: product.discount_percentage ?? '',
            unit: product.unit,
            stock: product.stock,
            expiration_date: product.expiration_date,
            is_featured_offer: product.is_featured_offer,
            is_active: product.is_active,
          })
          setCurrentImageUrl(product.image)
        } else if (categoryList.length > 0) {
          setForm((current) => ({ ...current, category: categoryList[0].name }))
        }
      } catch {
        setError('No se pudo cargar la información del producto.')
      } finally {
        setLoading(false)
      }
    }
    loadInitialData()
  }, [id, isEditing])

  function handleChange(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)

    try {
      let categoryName = form.category
      if (isNewCategory) {
        if (!newCategoryName.trim()) {
          setError('Escribe el nombre de la nueva categoría.')
          setSaving(false)
          return
        }
        const created = await createCategory(newCategoryName.trim())
        categoryName = created.name
      }

      if (!isEditing && !imageFile) {
        setError('Selecciona una imagen para el producto.')
        setSaving(false)
        return
      }

      const payload = new FormData()
      payload.append('name', form.name)
      payload.append('description', form.description)
      payload.append('category', categoryName)
      payload.append('price', form.price)
      if (form.discount_percentage !== '') {
        payload.append('discount_percentage', form.discount_percentage)
      }
      payload.append('unit', form.unit)
      payload.append('stock', form.stock)
      payload.append('expiration_date', form.expiration_date)
      payload.append('is_featured_offer', form.is_featured_offer)
      payload.append('is_active', form.is_active)
      if (imageFile) {
        payload.append('image', imageFile)
      }

      if (isEditing) {
        await updateProduct(id, payload)
      } else {
        await createProduct(payload)
      }
      navigate('/dashboard')
    } catch {
      setError('No se pudo guardar el producto. Revisa los datos e intenta de nuevo.')
      setSaving(false)
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <AdminHeader />

      <main className="max-w-2xl mx-auto px-6 py-8">
        <h1 className="text-xl font-semibold text-ink tracking-tight mb-6">
          {isEditing ? 'Editar producto' : 'Nuevo producto'}
        </h1>

        {loading && (
          <div className="flex items-center gap-2 text-ink-soft text-sm">
            <Loader2 className="w-4 h-4 animate-spin" />
            Cargando...
          </div>
        )}

        {!loading && (
          <form onSubmit={handleSubmit} className="bg-surface border border-line rounded-md p-6 space-y-4">
            <div>
              <label className={labelClass}>Nombre</label>
              <input
                required
                className={inputClass}
                value={form.name}
                onChange={(e) => handleChange('name', e.target.value)}
              />
            </div>

            <div>
              <label className={labelClass}>Descripción</label>
              <textarea
                rows={3}
                className={inputClass}
                value={form.description}
                onChange={(e) => handleChange('description', e.target.value)}
              />
            </div>

            <div>
              <label className={labelClass}>Categoría</label>
              {!isNewCategory ? (
                <select
                  className={inputClass}
                  value={form.category}
                  onChange={(e) => {
                    if (e.target.value === '__new__') {
                      setIsNewCategory(true)
                    } else {
                      handleChange('category', e.target.value)
                    }
                  }}
                >
                  {categories.map((cat) => (
                    <option
                      key={cat.id}
                      value={cat.name}
                      style={{ backgroundColor: 'var(--color-surface)', color: 'var(--color-ink)' }}
                    >
                      {cat.name}
                    </option>
                  ))}
                  <option
                    value="__new__"
                    style={{ backgroundColor: 'var(--color-surface)', color: 'var(--color-accent)' }}
                  >
                    + Nueva categoría
                  </option>
                </select>
              ) : (
                <div className="flex gap-2">
                  <input
                    autoFocus
                    className={inputClass}
                    placeholder="Nombre de la nueva categoría"
                    value={newCategoryName}
                    onChange={(e) => setNewCategoryName(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setIsNewCategory(false)}
                    className="px-3 text-xs text-ink-soft hover:text-ink border border-line rounded-sm"
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Precio (S/)</label>
                <input
                  required
                  type="number"
                  step="0.01"
                  min="0"
                  className={inputClass}
                  value={form.price}
                  onChange={(e) => handleChange('price', e.target.value)}
                />
              </div>
              <div>
                <label className={labelClass}>Descuento % (opcional)</label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  className={inputClass}
                  value={form.discount_percentage}
                  onChange={(e) => handleChange('discount_percentage', e.target.value)}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Unidad / presentación</label>
                <input
                  required
                  placeholder="Ej. 1 kg, pack x 6"
                  className={inputClass}
                  value={form.unit}
                  onChange={(e) => handleChange('unit', e.target.value)}
                />
              </div>
              <div>
                <label className={labelClass}>Stock</label>
                <input
                  required
                  type="number"
                  min="0"
                  className={inputClass}
                  value={form.stock}
                  onChange={(e) => handleChange('stock', e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className={labelClass}>Fecha de vencimiento</label>
              <input
                required
                type="date"
                className={inputClass}
                value={form.expiration_date}
                onChange={(e) => handleChange('expiration_date', e.target.value)}
              />
            </div>

            <div>
              <label className={labelClass}>Imagen</label>
              {currentImageUrl && !imageFile && (
                <img
                  src={currentImageUrl}
                  alt="Actual"
                  className="w-16 h-16 object-cover rounded-sm border border-line mb-2"
                />
              )}
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setImageFile(e.target.files?.[0] ?? null)}
                className="text-sm text-ink-soft"
              />
            </div>

            <div className="flex gap-6">
              <label className="flex items-center gap-2 text-sm text-ink">
                <input
                  type="checkbox"
                  checked={form.is_featured_offer}
                  onChange={(e) => handleChange('is_featured_offer', e.target.checked)}
                />
                Oferta especial
              </label>
              <label className="flex items-center gap-2 text-sm text-ink">
                <input
                  type="checkbox"
                  checked={form.is_active}
                  onChange={(e) => handleChange('is_active', e.target.checked)}
                />
                Activo
              </label>
            </div>

            {error && (
              <p className="text-sm text-error bg-error-soft border border-error/30 rounded-sm px-3 py-2">
                {error}
              </p>
            )}

            <div className="flex gap-3 pt-2">
              <button
                type="submit"
                disabled={saving}
                className="flex items-center gap-2 bg-accent hover:opacity-90 disabled:opacity-60 text-on-accent text-sm font-medium px-4 py-2 rounded-sm transition"
              >
                {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                {saving ? 'Guardando...' : 'Guardar'}
              </button>
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="text-sm text-ink-soft hover:text-ink px-4 py-2"
              >
                Cancelar
              </button>
            </div>
          </form>
        )}
      </main>
    </div>
  )
}
