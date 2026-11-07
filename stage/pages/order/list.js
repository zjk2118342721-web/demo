const api = require('../../utils/api')
const util = require('../../utils/util')

const timeText = (value) => (value ? String(value).slice(5, 16).replace('T', ' ') : '')

const TABS = [
  { label: '全部', value: -1 },
  { label: '待付款', value: 0 },
  { label: '待接单', value: 1 },
  { label: '制作中', value: 2 },
  { label: '待上菜', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

Page({
  data: {
    tabs: TABS,
    activeTab: -1,
    orders: [],
    loading: false
  },

  onShow() {
    const stored = wx.getStorageSync('wsx_order_tab')
    if (stored !== '' && stored !== undefined && stored !== null) {
      this.setData({ activeTab: Number(stored) })
      wx.removeStorageSync('wsx_order_tab')
    }
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders().finally(() => wx.stopPullDownRefresh())
  },

  switchTab(e) {
    const value = Number(e.currentTarget.dataset.value)
    if (value === this.data.activeTab) return
    this.setData({ activeTab: value })
    this.loadOrders()
  },

  loadOrders() {
    this.setData({ loading: true })
    const status = this.data.activeTab >= 0 ? this.data.activeTab : null
    return api
      .orders(status)
      .then((list) => {
        const orders = (list || []).map((order) => {
          const items = order.items || []
          const totalQty = items.reduce((sum, item) => sum + (item.quantity || 0), 0)
          let preview = '暂无菜品明细'
          if (items.length) {
            preview = items[0].dishName
            if (items.length > 1) {
              preview += ' 等' + items.length + '种菜品'
            }
          }
          return {
            ...order,
            statusClass: util.statusClass(order.status),
            statusText: util.statusText(order.status),
            totalText: util.money(order.totalAmount),
            timeText: timeText(order.createTime),
            preview,
            totalQty
          }
        })
        this.setData({ orders, loading: false })
      })
      .catch(() => this.setData({ loading: false }))
  },

  payOrder(e) {
    const id = Number(e.currentTarget.dataset.id)
    const order = this.findOrder(id)
    if (!order) return
    wx.showModal({
      title: '微信支付',
      content: '模拟支付 ¥' + order.totalText + '，确认后订单进入待接单状态。',
      confirmText: '确认支付',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .payOrder(id)
          .then(() => {
            wx.showToast({ title: '支付成功', icon: 'success' })
            this.loadOrders()
          })
          .catch(() => {})
      }
    })
  },

  cancelOrder(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.showModal({
      title: '取消订单',
      content: '确定要取消这笔订单吗？',
      confirmText: '取消订单',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .cancelOrder(id)
          .then(() => {
            wx.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrders()
          })
          .catch(() => {})
      }
    })
  },

  confirmOrder(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.showModal({
      title: '确认用餐完成',
      content: '确认已完成用餐并结束本次订单吗？',
      confirmText: '确认完成',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .confirmOrder(id)
          .then(() => {
            wx.showToast({ title: '订单已完成', icon: 'success' })
            this.loadOrders()
          })
          .catch(() => {})
      }
    })
  },

  goReview(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.navigateTo({ url: '/pages/review/review?orderId=' + id })
  },

  goDetail(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.navigateTo({ url: '/pages/order/detail?id=' + id })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  findOrder(id) {
    return this.data.orders.find((order) => order.id === id)
  }
})
