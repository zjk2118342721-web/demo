const api = require('../../utils/api')
const util = require('../../utils/util')

const STEP_LABELS = ['提交订单', '支付成功', '商家接单', '制作完成', '菜品上桌', '订单完成']
const DONE_INDEX = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 5 }

const timeText = (value) => (value ? String(value).slice(0, 16).replace('T', ' ') : '')

const starsOf = (rating) => {
  const score = Math.max(0, Math.min(5, Number(rating) || 0))
  return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, 5 - score)
}

Page({
  data: {
    id: null,
    order: {},
    items: [],
    steps: [],
    totalQty: 0,
    statusClass: '',
    statusText: '',
    statusDesc: '',
    createText: '',
    payText: '',
    completeText: '',
    reviewStars: ''
  },

  onLoad(options) {
    this.setData({ id: Number(options.id) })
  },

  onShow() {
    if (this.data.id) {
      this.loadOrder()
    }
  },

  onPullDownRefresh() {
    this.loadOrder().finally(() => wx.stopPullDownRefresh())
  },

  loadOrder() {
    return api
      .orderDetail(this.data.id)
      .then((order) => {
        const items = (order.items || []).map((item) => ({
          ...item,
          img: util.imageUrl(item.dishImage),
          priceText: util.money(item.price),
          subtotal: util.money(Number(item.price || 0) * (item.quantity || 0))
        }))
        const totalQty = items.reduce((sum, item) => sum + (item.quantity || 0), 0)
        const doneIndex = DONE_INDEX[order.status] === undefined ? 0 : DONE_INDEX[order.status]
        const steps = STEP_LABELS.map((label, index) => ({
          label,
          index,
          done: index <= doneIndex
        }))

        this.setData({
          order: {
            ...order,
            totalText: util.money(order.totalAmount)
          },
          items,
          totalQty,
          steps,
          statusClass: util.statusClass(order.status),
          statusText: util.statusText(order.status),
          statusDesc: util.statusDesc(order.status),
          createText: timeText(order.createTime),
          payText: timeText(order.payTime),
          completeText: timeText(order.completeTime),
          reviewStars: starsOf(order.reviewRating)
        })
      })
      .catch(() => {})
  },

  refresh() {
    this.loadOrder().then(() => wx.showToast({ title: '已刷新', icon: 'none' }))
  },

  payOrder() {
    wx.showModal({
      title: '微信支付',
      content: '模拟支付 ¥' + this.data.order.totalText + '，确认后订单进入待接单状态。',
      confirmText: '确认支付',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .payOrder(this.data.id)
          .then(() => {
            wx.showToast({ title: '支付成功', icon: 'success' })
            this.loadOrder()
          })
          .catch(() => {})
      }
    })
  },

  cancelOrder() {
    wx.showModal({
      title: '取消订单',
      content: '确定要取消这笔订单吗？',
      confirmText: '取消订单',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .cancelOrder(this.data.id)
          .then(() => {
            wx.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrder()
          })
          .catch(() => {})
      }
    })
  },

  confirmOrder() {
    wx.showModal({
      title: '确认用餐完成',
      content: '确认已完成用餐并结束本次订单吗？',
      confirmText: '确认完成',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .confirmOrder(this.data.id)
          .then(() => {
            wx.showToast({ title: '订单已完成', icon: 'success' })
            this.loadOrder()
          })
          .catch(() => {})
      }
    })
  },

  goReview() {
    wx.navigateTo({ url: '/pages/review/review?orderId=' + this.data.id })
  },

  goReviewList() {
    wx.switchTab({ url: '/pages/mine/mine' })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
