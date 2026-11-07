const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    tables: [],
    items: [],
    selectedTable: '',
    remark: '',
    totalText: '0.00',
    totalQty: 0,
    submitting: false
  },

  onLoad() {
    this.setData({ selectedTable: wx.getStorageSync('wsx_table_no') || '' })
    this.loadData()
  },

  loadData() {
    Promise.all([api.cartList(), api.tables()])
      .then(([cartList, tables]) => {
        let total = 0
        let totalQty = 0
        const items = (cartList || []).map((cart) => {
          const subtotal = Number(cart.price || 0) * (cart.quantity || 0)
          total += subtotal
          totalQty += cart.quantity || 0
          return {
            dishId: cart.dishId,
            dishName: cart.dishName,
            quantity: cart.quantity,
            img: util.imageUrl(cart.dishImage),
            subtotal: util.money(subtotal)
          }
        })

        const available = (tables || []).filter((t) => t.status === 0)
        let selectedTable = this.data.selectedTable
        if (!available.some((t) => t.tableNo === selectedTable)) {
          selectedTable = ''
        }

        this.setData({
          items,
          tables: tables || [],
          selectedTable,
          totalText: util.money(total),
          totalQty
        })

        if (!items.length) {
          wx.showToast({ title: '购物车为空，请先点餐', icon: 'none' })
        }
      })
      .catch(() => {})
  },

  selectTable(e) {
    const tableNo = e.currentTarget.dataset.no
    const status = Number(e.currentTarget.dataset.status)
    if (status === 1) {
      wx.showToast({ title: '该桌正在使用中', icon: 'none' })
      return
    }
    this.setData({ selectedTable: tableNo })
    wx.setStorageSync('wsx_table_no', tableNo)
  },

  onRemark(e) {
    this.setData({ remark: e.detail.value })
  },

  submit() {
    if (this.data.submitting) return
    if (!this.data.items.length) {
      wx.showToast({ title: '购物车为空，请先点餐', icon: 'none' })
      return
    }
    if (!this.data.selectedTable) {
      wx.showToast({ title: '请选择就餐桌号', icon: 'none' })
      return
    }

    const payload = {
      items: this.data.items.map((item) => ({
        dishId: item.dishId,
        quantity: item.quantity
      })),
      tableNo: this.data.selectedTable,
      remark: this.data.remark
    }

    this.setData({ submitting: true })
    api
      .createOrder(payload)
      .then((order) => {
        util.setCartBadge(0)
        wx.showToast({ title: '下单成功', icon: 'success' })
        setTimeout(() => {
          wx.redirectTo({ url: '/pages/order/detail?id=' + order.id })
        }, 800)
      })
      .catch(() => this.setData({ submitting: false }))
  }
})
