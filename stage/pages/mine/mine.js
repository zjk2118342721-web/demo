const api = require('../../utils/api')
const util = require('../../utils/util')

const starsOf = (rating) => {
  const score = Math.max(0, Math.min(5, Number(rating) || 0))
  return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, 5 - score)
}

const timeText = (value) => (value ? String(value).slice(0, 16).replace('T', ' ') : '')

Page({
  data: {
    user: {},
    avatarUrl: '',
    avatarText: '味',
    stats: [
      { label: '待付款', status: 0, count: 0 },
      { label: '待接单', status: 1, count: 0 },
      { label: '制作中', status: 2, count: 0 },
      { label: '已完成', status: 4, count: 0 }
    ],
    reviews: []
  },

  onShow() {
    this.loadUser()
    this.loadOrderStats()
    this.loadReviews()
  },

  onPullDownRefresh() {
    Promise.all([this.loadUser(), this.loadOrderStats(), this.loadReviews()]).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadUser() {
    return api
      .userInfo()
      .then((user) => {
        this.applyUser(user)
      })
      .catch(() => {
        const cached = wx.getStorageSync('wsx_user')
        if (cached) this.applyUser(cached)
      })
  },

  applyUser(user) {
    if (!user) return
    const avatar = user.avatar || ''
    this.setData({
      user,
      avatarUrl: avatar ? util.imageUrl(avatar) : '',
      avatarText: (user.nickname || '味').slice(0, 1)
    })
    wx.setStorageSync('wsx_user', user)
  },

  loadOrderStats() {
    return api
      .orders(null)
      .then((list) => {
        const counts = { 0: 0, 1: 0, 2: 0, 4: 0 }
        ;(list || []).forEach((order) => {
          if (counts[order.status] !== undefined) {
            counts[order.status] += 1
          }
        })
        const stats = this.data.stats.map((stat) => ({
          ...stat,
          count: counts[stat.status]
        }))
        this.setData({ stats })
      })
      .catch(() => {})
  },

  loadReviews() {
    return api
      .myReviews()
      .then((list) => {
        const reviews = (list || []).map((review) => ({
          ...review,
          stars: starsOf(review.rating),
          timeText: timeText(review.createTime)
        }))
        this.setData({ reviews })
      })
      .catch(() => {})
  },

  goOrders() {
    wx.setStorageSync('wsx_order_tab', -1)
    wx.switchTab({ url: '/pages/order/list' })
  },

  goStat(e) {
    const status = Number(e.currentTarget.dataset.status)
    wx.setStorageSync('wsx_order_tab', status)
    wx.switchTab({ url: '/pages/order/list' })
  },

  editNickname() {
    const current = this.data.user.nickname || ''
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: current ? '当前：' + current : '请输入新昵称',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        const nickname = (res.content || '').trim()
        if (!nickname) {
          wx.showToast({ title: '昵称不能为空', icon: 'none' })
          return
        }
        if (nickname.length > 20) {
          wx.showToast({ title: '昵称不能超过20个字', icon: 'none' })
          return
        }
        api
          .updateProfile({ nickname })
          .then((user) => {
            this.applyUser(user)
            wx.showToast({ title: '昵称已更新', icon: 'success' })
          })
          .catch(() => {})
      }
    })
  },

  onAvatarError() {
    this.setData({ avatarUrl: '' })
  },

  contactShop() {
    wx.showModal({
      title: '联系味食香',
      content: '门店电话：400-888-8888\n营业时间：10:00 - 21:30\n地址：味食香私房菜馆',
      showCancel: false,
      confirmText: '知道了',
      confirmColor: '#ef5a24'
    })
  },

  about() {
    wx.showModal({
      title: '关于味食香',
      content: '味食香微信点餐小程序 v1.0.0\n支持菜品浏览、分类筛选、购物车、在线下单、订单查询与菜品评价。',
      showCancel: false,
      confirmText: '知道了',
      confirmColor: '#ef5a24'
    })
  }
})
