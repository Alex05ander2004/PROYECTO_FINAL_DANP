import { useEffect, useMemo, useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { getMe } from '../api/authService'
import { getUsers, updateUser } from '../api/userService'
import AdminHeader from '../components/AdminHeader'
import TableSkeleton from '../components/TableSkeleton'
import SearchFilterBar from '../components/SearchFilterBar'

const ROLE_OPTIONS = [
  { value: 'CLIENT', label: 'Cliente' },
  { value: 'ADMIN', label: 'Administrador' },
]

const USER_FILTERS = [
  {
    key: 'role',
    label: 'Rol',
    options: ROLE_OPTIONS,
  },
  {
    key: 'active',
    label: 'Estado',
    options: [
      { value: 'yes', label: 'Solo activos' },
      { value: 'no', label: 'Solo inactivos' },
    ],
  },
]

const EMPTY_FILTERS = { role: null, active: null }

function formatDate(isoString) {
  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return isoString
  return date.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
}

export default function UsersPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)
  const [currentUserId, setCurrentUserId] = useState(null)
  const [search, setSearch] = useState('')
  const [activeFilters, setActiveFilters] = useState(EMPTY_FILTERS)

  useEffect(() => {
    async function fetchData() {
      try {
        const token = localStorage.getItem('access_token')
        const [userList, me] = await Promise.all([getUsers(), getMe(token)])
        setUsers(userList)
        setCurrentUserId(me.id)
      } catch {
        setError('No se pudieron cargar los usuarios.')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const filtered = useMemo(() => {
    const q = search.toLowerCase()
    return users.filter((u) => {
      if (q) {
        const matchesName = u.name?.toLowerCase().includes(q)
        const matchesEmail = u.email?.toLowerCase().includes(q)
        const matchesAddress = u.address?.toLowerCase().includes(q)
        if (!matchesName && !matchesEmail && !matchesAddress) return false
      }
      if (activeFilters.role && u.role !== activeFilters.role) return false
      if (activeFilters.active === 'yes' && !u.is_active) return false
      if (activeFilters.active === 'no' && u.is_active) return false
      return true
    })
  }, [users, search, activeFilters])

  function handleFilterChange(key, value) {
    setActiveFilters((prev) => ({ ...prev, [key]: value }))
  }

  async function applyUpdate(userId, fields) {
    const previous = users
    setUsers((current) => current.map((u) => (u.id === userId ? { ...u, ...fields } : u)))
    setUpdatingId(userId)
    try {
      await updateUser(userId, fields)
    } catch {
      setUsers(previous)
      setError('No se pudo actualizar el usuario.')
    } finally {
      setUpdatingId(null)
    }
  }

  function handleRoleChange(user, newRole) {
    if (newRole === 'ADMIN' && !window.confirm(`¿Convertir a "${user.name}" en administrador? Tendrá acceso completo a este panel.`)) {
      return
    }
    applyUpdate(user.id, { role: newRole })
  }

  function handleToggleActive(user) {
    applyUpdate(user.id, { is_active: !user.is_active })
  }

  return (
    <div className="min-h-screen bg-paper">
      <AdminHeader />

      <main className="max-w-6xl mx-auto px-6 py-8">
        <h1 className="text-xl font-semibold text-ink tracking-tight mb-6">Usuarios</h1>

        <SearchFilterBar
          search={search}
          onSearchChange={setSearch}
          searchPlaceholder="Buscar por nombre, correo o dirección..."
          filters={USER_FILTERS}
          activeFilters={activeFilters}
          onFilterChange={handleFilterChange}
        />

        {loading && <TableSkeleton columns={7} />}

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
                  <th className="px-4 py-3 font-medium">Nombre</th>
                  <th className="px-4 py-3 font-medium">Correo</th>
                  <th className="px-4 py-3 font-medium">Teléfono</th>
                  <th className="px-4 py-3 font-medium">Dirección</th>
                  <th className="px-4 py-3 font-medium">Registrado</th>
                  <th className="px-4 py-3 font-medium">Rol</th>
                  <th className="px-4 py-3 font-medium">Estado</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((user) => {
                  const isSelf = user.id === currentUserId
                  return (
                    <tr key={user.id} className="border-t border-line hover:bg-surface-sunken/60">
                      <td className="px-4 py-3 font-medium text-ink">
                        {user.name} {isSelf && <span className="text-xs text-ink-soft">(tú)</span>}
                      </td>
                      <td className="px-4 py-3 text-ink-soft">{user.email}</td>
                      <td className="px-4 py-3 text-ink-soft">{user.phone || '—'}</td>
                      <td className="px-4 py-3 text-ink-soft max-w-[200px] truncate">{user.address || '—'}</td>
                      <td className="px-4 py-3 text-ink-soft">{formatDate(user.date_joined)}</td>
                      <td className="px-4 py-3">
                        <div className="relative w-40">
                          <select
                            value={user.role}
                            disabled={isSelf || updatingId === user.id}
                            onChange={(e) => handleRoleChange(user, e.target.value)}
                            className="w-full appearance-none bg-transparent border border-line rounded-sm text-xs text-ink pl-2 pr-7 py-1.5 focus:outline-none focus:border-accent disabled:opacity-60"
                          >
                            {ROLE_OPTIONS.map((option) => (
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
                      </td>
                      <td className="px-4 py-3">
                        <button
                          disabled={isSelf || updatingId === user.id}
                          onClick={() => handleToggleActive(user)}
                          className="inline-flex items-center gap-1.5 text-xs font-medium text-ink disabled:opacity-60 hover:opacity-80"
                        >
                          <span
                            className={`w-1.5 h-1.5 rounded-full ${user.is_active ? 'bg-accent' : 'bg-ink-soft'}`}
                          />
                          {user.is_active ? 'Activo' : 'Inactivo'}
                        </button>
                      </td>
                    </tr>
                  )
                })}

                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-4 py-6 text-center text-ink-soft">
                      {users.length === 0
                        ? 'No hay usuarios registrados.'
                        : 'Ningún usuario coincide con la búsqueda.'}
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
