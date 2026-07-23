import { NavLink, useNavigate } from 'react-router-dom'
import { LogOut, Leaf } from 'lucide-react'

const NAV_LINKS = [
  { to: '/products', label: 'Productos' },
  { to: '/orders', label: 'Pedidos' },
  { to: '/users', label: 'Usuarios' },
]

export default function AdminHeader() {
  const navigate = useNavigate()

  function handleLogout() {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    navigate('/login')
  }

  return (
    <header className="border-b border-line bg-surface">
      <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <div className="flex items-center gap-2">
            <Leaf className="w-5 h-5 text-accent" />
            <span className="font-semibold text-ink tracking-tight">ReFood — Admin</span>
          </div>
          <nav className="flex items-center gap-5">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `text-sm font-medium transition ${
                    isActive ? 'text-accent' : 'text-ink-soft hover:text-ink'
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 text-sm text-ink-soft hover:text-ink transition"
        >
          <LogOut className="w-4 h-4" />
          Cerrar sesión
        </button>
      </div>
    </header>
  )
}
