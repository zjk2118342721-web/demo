const { BASE_URL } = require('./config')

const money = (value) => Number(value || 0).toFixed(2)

const imageUrl = (path) => {
  if (!path) return ''
  if (path.indexOf('http') === 0) return path
  return BASE_URL + path
}

const STATUS_MAP = {
  0: { text: '待付款', desc: '请尽快完成支付，支付后餐厅开始接单' },
  1: { text: '待接单', desc: '已支付，等待餐厅确认接单' },
  2: { text: '制作中', desc: '后厨正在制作中，请耐心等待' },
  3: { text: '待上菜', desc: '菜品已出锅，即将为您上菜' },
  4: { text: '已完成', desc: '订单已完成，感谢您的光临' },
  5: { text: '已取消', desc: '订单已取消' }
}

const statusText = (status) => (STATUS_MAP[status] || {}).text || '未知状态'
const statusDesc = (status) => (STATUS_MAP[status] || {}).desc || ''

const statusClass = (status) => {
  if (status === 0) return 'pending'
  if (status === 1) return 'waiting'
  if (status === 2) return 'cooking'
  if (status === 3) return 'serving'
  if (status === 4) return 'finished'
  return 'cancelled'
}

const parseImages = (images) => (images ? images.split(',').filter(Boolean) : [])

const setCartBadge = (count) => {
  const n = Number(count) || 0
  if (n > 0) {
    wx.setTabBarBadge({ index: 1, text: n > 99 ? '99+' : String(n), fail: () => {} })
  } else {
    wx.removeTabBarBadge({ index: 1, fail: () => {} })
  }
}

module.exports = {
  money,
  imageUrl,
  statusText,
  statusDesc,
  statusClass,
  parseImages,
  setCartBadge
}
