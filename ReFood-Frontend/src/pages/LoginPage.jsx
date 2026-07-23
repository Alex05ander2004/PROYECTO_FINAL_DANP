import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Mail, Lock, Loader2, Leaf } from 'lucide-react'
import { login, getMe } from '../api/authService'
import ThemeToggle from '../components/ThemeToggle'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const token = localStorage.getItem('access_token')
    if (token) {
      navigate('/products', { replace: true })
    }
  }, [navigate])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const { access, refresh } = await login(email, password)
      const user = await getMe(access)

      if (user.role !== 'ADMIN') {
        setError('Esta cuenta no tiene permisos de administrador.')
        setLoading(false)
        return
      }

      localStorage.setItem('access_token', access)
      localStorage.setItem('refresh_token', refresh)
      navigate('/products')
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Correo o contraseña incorrectos.')
      } else {
        setError('Ocurrió un error. Intenta de nuevo.')
      }
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4 relative">
      <div className="absolute top-4 right-4">
        <ThemeToggle />
      </div>
      <div className="w-full max-w-sm bg-surface border border-line rounded-lg p-8">
        <div className="flex flex-col items-center mb-6">
          <div className="w-14 h-14 rounded-full bg-accent-soft flex items-center justify-center mb-3">
            <Leaf className="w-6 h-6 text-accent" />
          </div>
          <h1 className="text-xl font-semibold text-ink tracking-tight">ReFood</h1>
          <p className="text-sm text-ink-soft">Panel de administración</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-ink-soft mb-1">Correo</label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-soft" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full pl-9 pr-3 py-2.5 bg-transparent border border-line rounded-sm text-sm text-ink placeholder:text-ink-soft/70 focus:outline-none focus:border-accent"
                placeholder="admin@refood.com"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-ink-soft mb-1">Contraseña</label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-soft" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full pl-9 pr-3 py-2.5 bg-transparent border border-line rounded-sm text-sm text-ink placeholder:text-ink-soft/70 focus:outline-none focus:border-accent"
                placeholder="••••••••"
              />
            </div>
          </div>

          {error && (
            <p className="text-sm text-error bg-error-soft border border-error/30 rounded-sm px-3 py-2">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 bg-accent hover:opacity-90 disabled:opacity-60 text-on-accent text-sm font-medium py-2.5 rounded-sm transition"
          >
            {loading && <Loader2 className="w-4 h-4 animate-spin" />}
            {loading ? 'Ingresando...' : 'Iniciar sesión'}
          </button>
        </form>
      </div>
    </div>
  )
}
