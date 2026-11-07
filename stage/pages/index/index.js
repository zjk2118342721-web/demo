const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    banner: util.imageUrl('/uploads/banner.jpg'),
    categories: [{ id: 0, name: '全部' }],
    activeCategory: 0,
    keyword: '',
    dishes: [],
    cartMap: {},
    cartCount: 0,
    cartTotalText: '0.00',
    loading: false,
    tableNo: ''
  },

  onLoad() {
    this.searchTimer = null
    this.loadCategories()
    this.loadDishes()
  },

  onShow() {
    const tableNo = wx.getStorageSync('wsx_table_no') || ''
    this.setData({ tableNo })
    this.refreshCart()
  },

  onPullDownRefresh() {
    Promise.all([this.loadCategories(), this.loadDishes(), this.refreshCart()]).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadCategories() {
    return api
      .categories()
      .then((list) => {
        const categories = [{ id: 0, name: '全部' }].concat(
          (list || []).map((item) => ({ id: item.id, name: item.name }))
        )
        this.setData({ categories })
      })
      .catch(() => {})
  },

  loadDishes() {
    const params = {}
    if (this.data.activeCategory) {
      params.categoryId = this.data.activeCategory
    }
    if (this.data.keyword) {
      params.keyword = this.data.keyword.trim()
    }
    this.setData({ loading: true })
    return api
      .dishes(params)
      .then((list) => {
        const cartMap = this.data.cartMap
        const dishes = (list || []).map((dish) => ({
          ...dish,
          img: util.imageUrl(dish.image),
          priceText: util.money(dish.price),
          originalText:
            dish.originalPrice && Number(dish.originalPrice) > 0 ? util.money(dish.originalPrice) : '',
          qty: cartMap[dish.id] || 0
        }))
        this.setData({ dishes, loading: false })
      })
      .catch(() => this.setData({ loading: false }))
  },

  refreshCart() {
    return api
      .cartList()
      .then((list) => {
        const cartMap = {}
        let count = 0
        let total = 0
        ;(list || []).forEach((cart) => {
          cartMap[cart.dishId] = cart.quantity
          count += cart.quantity || 0
          total += Number(cart.price || 0) * (cart.quantity || 0)
        })
        const dishes = this.data.dishes.map((dish) => ({
          ...dish,
          qty: cartMap[dish.id] || 0
        }))
        this.setData({
          cartMap,
          cartCount: count,
          cartTotalText: util.money(total),
          dishes
        })
        util.setCartBadge(count)
      })
      .catch(() => {})
  },

  onCategoryTap(e) {
    const id = Number(e.currentTarget.dataset.id)
    if (id === this.data.activeCategory) return
    this.setData({ activeCategory: id })
    this.loadDishes()
  },

  onSearchInput(e) {
    const keyword = e.detail.value || ''
    this.setData({ keyword })
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
    this.searchTimer = setTimeout(() => this.loadDishes(), 300)
  },

  onSearchConfirm() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
    this.loadDishes()
  },

  clearSearch() {
    this.setData({ keyword: '' })
    this.loadDishes()
  },

  addDish(e) {
    const id = Number(e.currentTarget.dataset.id)
    api
      .cartAdd(id, 1)
      .then(() => this.refreshCart())
      .catch(() => {})
  },

  minusDish(e) {
    const id = Number(e.currentTarget.dataset.id)
    const qty = (this.data.cartMap[id] || 0) - 1
    const action = qty <= 0 ? api.cartRemove(id) : api.cartUpdate(id, qty)
    action.then(() => this.refreshCart()).catch(() => {})
  },

  goDetail(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.navigateTo({ url: '/pages/dish/detail?id=' + id })
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  },

  noop() {}
})
