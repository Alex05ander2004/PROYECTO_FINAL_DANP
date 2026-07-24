import { X, SlidersHorizontal, ChevronDown } from 'lucide-react'
import { useState, useRef, useEffect } from 'react'

/* Barra de búsqueda + filtros */
export default function SearchFilterBar({
  search,
  onSearchChange,
  searchPlaceholder = 'Buscar...',
  filters = [],
  activeFilters = {},
  onFilterChange,
}) {
  const [panelOpen, setPanelOpen] = useState(false)
  const panelRef = useRef(null)

  useEffect(() => {
    function handleClickOutside(e) {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setPanelOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const activeChips = Object.entries(activeFilters)
    .filter(([, v]) => v !== null && v !== undefined)
    .map(([key, value]) => {
      const filterDef = filters.find((f) => f.key === key)
      const optionDef = filterDef?.options.find((o) => o.value === value)
      return { key, label: `${filterDef?.label}: ${optionDef?.label ?? value}` }
    })

  return (
    <div className="flex flex-col gap-3 mb-4">
      {/* Barra principal */}
      <div className="flex items-center gap-2">
        {/* Input búsqueda */}
        <div className="relative flex-1">
          <input
            type="text"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder={searchPlaceholder}
            className="w-full pl-3 pr-8 py-2 bg-surface border border-line rounded-md text-sm text-ink placeholder:text-ink-soft/60 focus:outline-none focus:border-accent transition"
          />
          {search && (
            <button
              onClick={() => onSearchChange('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-ink-soft hover:text-ink"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Botón de filtros */}
        {filters.length > 0 && (
          <div className="relative" ref={panelRef}>
            <button
              onClick={() => setPanelOpen((prev) => !prev)}
              className={`flex items-center gap-2 px-3 py-2 border rounded-md text-sm transition ${activeChips.length > 0
                  ? 'bg-accent/10 border-accent text-accent'
                  : 'bg-surface border-line text-ink-soft hover:text-ink hover:border-accent/50'
                }`}
            >
              <SlidersHorizontal className="w-4 h-4" />
              Filtros
              {activeChips.length > 0 && (
                <span className="bg-accent text-on-accent text-xs rounded-full w-4 h-4 flex items-center justify-center font-medium">
                  {activeChips.length}
                </span>
              )}
              <ChevronDown className={`w-3.5 h-3.5 transition-transform ${panelOpen ? 'rotate-180' : ''}`} />
            </button>

            {/* Panel desplegable */}
            {panelOpen && (
              <div className="absolute right-0 mt-2 w-64 bg-surface border border-line rounded-md shadow-lg p-4 z-40 space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-ink-soft uppercase tracking-wide">Filtros</span>
                  {activeChips.length > 0 && (
                    <button
                      onClick={() => {
                        filters.forEach((f) => onFilterChange(f.key, null))
                      }}
                      className="text-xs text-error hover:opacity-80"
                    >
                      Limpiar todo
                    </button>
                  )}
                </div>

                {filters.map((filter) => (
                  <div key={filter.key}>
                    <label className="block text-xs font-medium text-ink-soft mb-1.5">
                      {filter.label}
                    </label>
                    <div className="flex flex-wrap gap-1.5">
                      {filter.options.map((opt) => {
                        const isActive = activeFilters[filter.key] === opt.value
                        return (
                          <button
                            key={opt.value}
                            onClick={() =>
                              onFilterChange(filter.key, isActive ? null : opt.value)
                            }
                            className={`px-2.5 py-1 rounded-full text-xs font-medium transition-all ${isActive
                                ? 'bg-accent text-on-accent'
                                : 'bg-surface-sunken text-ink-soft hover:text-ink hover:bg-line'
                              }`}
                          >
                            {opt.label}
                          </button>
                        )
                      })}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Chips de filtros activos */}
      {activeChips.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {activeChips.map((chip) => (
            <span
              key={chip.key}
              className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-accent/10 border border-accent/30 text-accent text-xs font-medium rounded-full"
            >
              {chip.label}
              <button
                onClick={() => onFilterChange(chip.key, null)}
                className="hover:opacity-70 transition"
                aria-label={`Quitar filtro ${chip.label}`}
              >
                <X className="w-3 h-3" />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
