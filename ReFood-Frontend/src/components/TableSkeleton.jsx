export default function TableSkeleton({ columns = 5, rows = 6 }) {
  return (
    <div className="bg-surface border border-line rounded-md overflow-hidden animate-pulse">
      <div className="px-4 py-3 border-b border-line flex gap-6">
        {Array.from({ length: columns }).map((_, i) => (
          <div key={i} className="h-3 bg-surface-sunken rounded-sm flex-1" />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, r) => (
        <div
          key={r}
          className="px-4 py-4 border-b border-line last:border-b-0 flex gap-6 items-center"
        >
          {Array.from({ length: columns }).map((_, c) => (
            <div
              key={c}
              className="h-4 bg-surface-sunken rounded-sm flex-1"
              style={{ opacity: 1 - r * 0.08 }}
            />
          ))}
        </div>
      ))}
    </div>
  )
}
