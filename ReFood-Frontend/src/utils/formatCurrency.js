export function formatCurrency(value) {
  const numericValue = Number(value ?? 0)
  if (Number.isNaN(numericValue)) {
    return 'S/.00.00'
  }

  const truncatedValue = Math.floor(numericValue * 10) / 10
  const [wholePart, decimalPart = '0'] = truncatedValue.toFixed(1).split('.')
  const formattedWholePart = wholePart.replace(/\B(?=(\d{3})+(?!\d))/g, '.')

  return `S/.${formattedWholePart}.${decimalPart}0`
}
