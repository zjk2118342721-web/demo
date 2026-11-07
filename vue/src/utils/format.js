export const ORDER_STATUS = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待接单', type: 'danger' },
  2: { text: '制作中', type: 'primary' },
  3: { text: '待上菜', type: 'success' },
  4: { text: '已完成', type: 'info' },
  5: { text: '已取消', type: 'info' }
}

export const orderStatusText = (status) => ORDER_STATUS[status]?.text || '未知'
export const orderStatusType = (status) => ORDER_STATUS[status]?.type || 'info'

export const money = (value) => {
  const num = Number(value || 0)
  return num.toFixed(2)
}

export const assetUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return path
}
