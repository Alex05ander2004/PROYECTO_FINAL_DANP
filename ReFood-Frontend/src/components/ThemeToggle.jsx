import { Sun, Moon, Monitor } from 'lucide-react'
import { useTheme } from '../context/ThemeContext'
import { useState, useRef, useEffect } from 'react'

export default function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef(null)

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const options = [
    { value: 'light', label: 'Claro', icon: Sun },
    { value: 'dark', label: 'Oscuro', icon: Moon },
    { value: 'system', label: 'Sistema', icon: Monitor },
  ]

  const currentOption = options.find((opt) => opt.value === theme) || options[2]
  const CurrentIcon = currentOption.icon

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="p-2 rounded-sm text-ink-soft hover:bg-surface-sunken hover:text-ink transition-colors flex items-center justify-center"
        aria-label="Cambiar tema"
      >
        <CurrentIcon className="w-5 h-5" />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-36 bg-surface border border-line rounded-md shadow-lg py-1 z-50">
          {options.map((opt) => {
            const Icon = opt.icon
            return (
              <button
                key={opt.value}
                onClick={() => {
                  setTheme(opt.value)
                  setIsOpen(false)
                }}
                className={`w-full text-left px-4 py-2 text-sm flex items-center gap-3 hover:bg-surface-sunken transition-colors ${
                  theme === opt.value ? 'text-accent font-medium' : 'text-ink'
                }`}
              >
                <Icon className="w-4 h-4" />
                {opt.label}
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
