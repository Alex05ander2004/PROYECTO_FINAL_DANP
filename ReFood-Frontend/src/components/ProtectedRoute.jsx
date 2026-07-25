import { useEffect, useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { validateStoredSession } from '../api/authService'

export default function ProtectedRoute({ children }) {
  const [isChecking, setIsChecking] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const location = useLocation()

  useEffect(() => {
    let isMounted = true

    async function verifySession() {
      const valid = await validateStoredSession()
      if (isMounted) {
        setIsAuthenticated(valid)
        setIsChecking(false)
      }
    }

    verifySession()

    return () => {
      isMounted = false
    }
  }, [])

  if (isChecking) {
    return (
      <div className="min-h-screen bg-paper flex items-center justify-center">
        <Loader2 className="w-6 h-6 animate-spin text-accent" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}
