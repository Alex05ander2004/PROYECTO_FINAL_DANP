import { X, MapPin, CreditCard, Clock, FileText } from 'lucide-react'
import { formatCurrency } from '../utils/formatCurrency'

const PAYMENT_LABELS = {
  EFECTIVO: 'Efectivo',
  YAPE: 'Yape',
  PLIN: 'Plin',
  TARJETA: 'Tarjeta',
}

function formatDate(isoString) {
  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return isoString
  return date.toLocaleString('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function OrderDetailModal({ order, statusMeta, onClose }) {
  if (!order) return null

  const meta = statusMeta(order.status)
  const items = order.items || []

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
      onClick={onClose}
    >
      <div
        className="w-full max-w-lg max-h-[85vh] overflow-y-auto rounded-md border border-line bg-surface shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between border-b border-line px-5 py-4">
          <div>
            <h2 className="text-lg font-semibold text-ink">Pedido #{order.id}</h2>
            <div className="mt-1 flex items-center gap-1.5 text-xs text-ink-soft">
              <Clock className="w-3.5 h-3.5" />
              {formatDate(order.created_at)}
            </div>
          </div>
          <button
            onClick={onClose}
            className="rounded-sm p-1.5 text-ink-soft transition hover:bg-surface-sunken hover:text-ink"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="space-y-4 px-5 py-4">
          <div className="flex items-center gap-2">
            {meta.icon && <meta.icon className="w-4 h-4" style={{ color: meta.color }} />}
            <span className="text-sm font-medium text-ink">{meta.label}</span>
          </div>

          <div className="space-y-2 rounded-sm border border-line bg-surface-sunken/40 px-3 py-3 text-sm">
            <div>
              <div className="text-xs font-medium text-ink-soft">Cliente</div>
              <div className="text-ink">{order.user_name}</div>
              <div className="text-xs text-ink-soft">{order.user_email}</div>
            </div>

            <div className="flex items-start gap-2 pt-1">
              <MapPin className="w-4 h-4 shrink-0 text-ink-soft mt-0.5" />
              <div>
                <div className="text-xs font-medium text-ink-soft">Dirección de entrega</div>
                <div className="text-ink">{order.delivery_address || '—'}</div>
              </div>
            </div>

            <div className="flex items-start gap-2 pt-1">
              <CreditCard className="w-4 h-4 shrink-0 text-ink-soft mt-0.5" />
              <div>
                <div className="text-xs font-medium text-ink-soft">Método de pago</div>
                <div className="text-ink">
                  {PAYMENT_LABELS[order.payment_method] ?? order.payment_method}
                  {order.payment_reference && (
                    <span className="text-ink-soft">
                      {' '}
                      {order.payment_method === 'TARJETA'
                        ? `(•••• ${order.payment_reference})`
                        : `(Op: ${order.payment_reference})`}
                    </span>
                  )}
                </div>
              </div>
            </div>

            {order.notes && (
              <div className="flex items-start gap-2 pt-1">
                <FileText className="w-4 h-4 shrink-0 text-ink-soft mt-0.5" />
                <div>
                  <div className="text-xs font-medium text-ink-soft">Notas</div>
                  <div className="text-ink">{order.notes}</div>
                </div>
              </div>
            )}
          </div>

          <div>
            <h3 className="mb-2 text-sm font-medium text-ink">Productos</h3>
            <div className="divide-y divide-line rounded-sm border border-line">
              {items.length > 0 ? (
                items.map((item) => (
                  <div key={item.id} className="flex items-center justify-between px-3 py-2 text-sm">
                    <div>
                      <div className="text-ink">{item.product_name}</div>
                      <div className="text-xs text-ink-soft">
                        {item.quantity} x {formatCurrency(item.unit_price)}
                      </div>
                    </div>
                    <div className="font-medium text-ink">
                      {formatCurrency(item.quantity * Number(item.unit_price))}
                    </div>
                  </div>
                ))
              ) : (
                <div className="px-3 py-3 text-sm text-ink-soft">Sin productos.</div>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between border-t border-line pt-3 text-base">
            <span className="font-medium text-ink">Total</span>
            <span className="font-semibold text-ink">{formatCurrency(order.total)}</span>
          </div>
        </div>
      </div>
    </div>
  )
}