import { Link } from 'react-router-dom'
import { Home } from 'lucide-react'
import AdminHeader from '../components/AdminHeader'

function NotFoundPage() {
  return (
    <div className="min-h-screen bg-paper">
      <main className="max-w-3xl mx-auto px-6 py-16">
        <div className="bg-surface border border-line rounded-md shadow-sm px-8 py-12 text-center">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-accent">Error 404</p>
          <h1 className="mt-3 text-5xl font-semibold text-ink tracking-tight">Página no encontrada</h1>
          <p className="mt-4 text-base text-ink-soft max-w-xl mx-auto">
            La URL que intentas abrir no existe o ya no está disponible.
          </p>

          <div className="mt-8 flex justify-center">
            <Link
              to="/login"
              className="inline-flex items-center gap-2 bg-accent hover:opacity-90 text-on-accent text-sm font-medium px-4 py-2 rounded-sm transition"
            >
              <Home className="w-4 h-4" />
              Ir a inicio
            </Link>
          </div>
        </div>
      </main>
    </div>
  )
}

export default NotFoundPage
