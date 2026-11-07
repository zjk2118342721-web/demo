const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    items: [],
    totalText: '0.00',
    totalQty: 0,
    loading: false
  },

  onShow() {
    this.loadCart()
  },

  onPullDownRefresh() {
    this.loadCart().finally(() => wx.stopPullDownRefresh())
  },

  loadCart() {
    this.setData({ loading: true })
    return api
      .cartList()
      .then((list) => {
        let total = 0
        let totalQty = 0
        const items = (list || []).map((cart) => {
          const subtotal = Number(cart.price || 0) * (cart.quantity || 0)
          total += subtotal
          totalQty += cart.quantity || 0
          return {
            ...cart,
            img: util.imageUrl(cart.dishImage),
            priceText: util.money(cart.price),
            subtotal: util.money(subtotal)
          }
        })
        this.setData({
          items,
          totalText: util.money(total),
          totalQty,
          loading: false
        })
        util.setCartBadge(totalQty)
      })
      .catch(() => this.setData({ loading: false }))
  },

  findItem(dishId) {
    return this.data.items.find((item) => item.dishId === dishId)
  },

  minus(e) {
    const dishId = Number(e.currentTarget.dataset.id)
    const item = this.findItem(dishId)
    if (!item) return
    const qty = item.quantity - 1
    const action = qty <= 0 ? api.cartRemove(dishId) : api.cartUpdate(dishId, qty)
    action.then(() => this.loadCart()).catch(() => {})
  },

  plus(e) {
    const dishId = Number(e.currentTarget.dataset.id)
    const item = this.findItem(dishId)
    if (!item || item.quantity >= 99) return
    api
      .cartUpdate(dishId, item.quantity + 1)
      .then(() => this.loadCart())
      .catch(() => {})
  },

  removeItem(e) {
    const dishId = Number(e.currentTarget.dataset.id)
    const item = this.findItem(dishId)
    wx.showModal({
      title: '移出购物车',
      content: '确定将「' + (item ? item.dishName : '该菜品') + '」移出购物车吗？',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .cartRemove(dishId)
          .then(() => this.loadCart())
          .catch(() => {})
      }
    })
  },

  clearAll() {
    wx.showModal({
      title: '清空购物车',
      content: '确定清空购物车中的全部菜品吗？',
      confirmColor: '#ef5a24',
      success: (res) => {
        if (!res.confirm) return
        api
          .cartClear()
          .then(() => this.loadCart())
          .catch(() => {})
      }
    })
  },

  checkout() {
    if (!this.data.items.length) {
      wx.showToast({ title: '购物车还是空的', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/order/confirm' })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
