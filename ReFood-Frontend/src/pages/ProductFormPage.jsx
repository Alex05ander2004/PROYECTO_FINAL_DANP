import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronDown, Loader2, X } from 'lucide-react'
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
const selectClass =
  'w-full appearance-none px-3 py-2 pr-9 bg-transparent border border-line rounded-sm text-sm text-ink focus:outline-none focus:border-accent'
const labelClass = 'block text-xs font-medium text-ink-soft mb-1'

// Bloquea teclas validas para <input type="number"> pero indeseadas aca
// (+, -, e/E de notacion cientifica); el navegador las deja escribir aunque
// min="0" solo se valida al enviar el formulario, no al tipear.
function blockKeys(keys) {
  return (e) => {
    if (keys.includes(e.key)) e.preventDefault()
  }
}
const blockIntegerKeys = blockKeys(['e', 'E', '+', '-', '.', ','])
const blockDecimalKeys = blockKeys(['e', 'E', '+', '-'])

function sanitizeInteger(value) {
  return value.replace(/[^0-9]/g, '')
}
function sanitizeDecimal(value) {
  const cleaned = value.replace(/[^0-9.]/g, '')
  const firstDot = cleaned.indexOf('.')
  if (firstDot === -1) return cleaned
  return cleaned.slice(0, firstDot + 1) + cleaned.slice(firstDot + 1).replace(/\./g, '')
}

function validateForm(form, { isEditing, isNewCategory, newCategoryName, hasImage }) {
  if (!form.name.trim()) return 'El nombre es obligatorio.'
  if (isNewCategory && !newCategoryName.trim()) return 'Escribe el nombre de la nueva categoría.'
  if (!isNewCategory && !form.category) return 'Selecciona una categoría.'

  const price = Number(form.price)
  if (form.price === '' || Number.isNaN(price) || price <= 0) {
    return 'El precio debe ser un número mayor a 0.'
  }

  if (form.discount_percentage !== '') {
    const discount = Number(form.discount_percentage)
    if (Number.isNaN(discount) || discount < 0 || discount > 100) {
      return 'El descuento debe ser un número entre 0 y 100.'
    }
  }

  if (!form.unit.trim()) return 'La unidad / presentación es obligatoria.'

  const stock = Number(form.stock)
  if (form.stock === '' || Number.isNaN(stock) || stock < 0 || !Number.isInteger(stock)) {
    return 'El stock debe ser un número entero mayor o igual a 0.'
  }

  if (!form.expiration_date) return 'La fecha de vencimiento es obligatoria.'
  if (!isEditing && !hasImage) return 'Selecciona una imagen para el producto.'

  return null
}

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
  const fileInputRef = useRef(null)

  function handleClearImage() {
    setImageFile(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

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

    const validationError = validateForm(form, {
      isEditing,
      isNewCategory,
      newCategoryName,
      hasImage: Boolean(imageFile),
    })
    if (validationError) {
      setError(validationError)
      return
    }

    setSaving(true)

    try {
      let categoryName = form.category
      if (isNewCategory) {
        const created = await createCategory(newCategoryName.trim())
        categoryName = created.name
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
                <div className="relative">
                  <select
                    className={selectClass}
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
                  <ChevronDown className="w-4 h-4 text-ink-soft absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
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
                  inputMode="decimal"
                  className={inputClass}
                  value={form.price}
                  onKeyDown={blockDecimalKeys}
                  onChange={(e) => handleChange('price', sanitizeDecimal(e.target.value))}
                />
              </div>
              <div>
                <label className={labelClass}>Descuento % (opcional)</label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  inputMode="numeric"
                  className={inputClass}
                  value={form.discount_percentage}
                  onKeyDown={blockIntegerKeys}
                  onChange={(e) =>
                    handleChange(
                      'discount_percentage',
                      sanitizeInteger(e.target.value).slice(0, 3)
                    )
                  }
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
                  inputMode="numeric"
                  className={inputClass}
                  value={form.stock}
                  onKeyDown={blockIntegerKeys}
                  onChange={(e) => handleChange('stock', sanitizeInteger(e.target.value))}
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
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="px-3 py-2 border border-line rounded-sm text-sm text-ink hover:bg-surface-sunken transition"
                >
                  {currentImageUrl || imageFile ? 'Cambiar imagen' : 'Seleccionar imagen'}
                </button>
                {imageFile && (
                  <>
                    <span className="text-xs text-ink-soft truncate max-w-[160px]">{imageFile.name}</span>
                    <button
                      type="button"
                      onClick={handleClearImage}
                      className="p-1.5 rounded-sm hover:bg-error-soft text-error"
                      aria-label="Quitar imagen seleccionada"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={(e) => setImageFile(e.target.files?.[0] ?? null)}
                className="hidden"
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
