const api = require('../../utils/api')
const util = require('../../utils/util')

const LABELS = ['', '非常差', '不太满意', '一般般', '满意', '非常满意']

Page({
  data: {
    orderId: null,
    starList: [1, 2, 3, 4, 5],
    rating: 5,
    ratingLabel: LABELS[5],
    content: '',
    anonymous: false,
    submitting: false,
    orderNo: '',
    tableNo: '',
    totalText: '0.00',
    dishNames: ''
  },

  onLoad(options) {
    const orderId = Number(options.orderId)
    this.setData({ orderId })
    if (!orderId) {
      wx.showToast({ title: '订单信息有误', icon: 'none' })
      return
    }
    this.loadOrder()
  },

  loadOrder() {
    api
      .orderDetail(this.data.orderId)
      .then((order) => {
        const names = (order.items || []).map((item) => item.dishName + ' ×' + item.quantity)
        this.setData({
          orderNo: order.orderNo,
          tableNo: order.tableNo,
          totalText: util.money(order.totalAmount),
          dishNames: names.join('、')
        })
      })
      .catch(() => {})
  },

  onStarTap(e) {
    const rating = Number(e.currentTarget.dataset.value)
    this.setData({ rating, ratingLabel: LABELS[rating] })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onAnonymousChange(e) {
    this.setData({ anonymous: e.detail.value })
  },

  submit() {
    if (this.data.submitting) return
    if (!this.data.orderId) {
      wx.showToast({ title: '订单信息有误', icon: 'none' })
      return
    }
    if (!this.data.rating) {
      wx.showToast({ title: '请选择评分', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    api
      .addReview({
        orderId: this.data.orderId,
        rating: this.data.rating,
        content: this.data.content,
        anonymous: this.data.anonymous ? 1 : 0
      })
      .then(() => {
        wx.showToast({ title: '评价成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 800)
      })
      .catch(() => this.setData({ submitting: false }))
  }
})
