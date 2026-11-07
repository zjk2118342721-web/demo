const api = require('../../utils/api')
const util = require('../../utils/util')

const starsOf = (rating) => {
  const score = Math.max(0, Math.min(5, Number(rating) || 0))
  return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, 5 - score)
}

const timeText = (value) => (value ? String(value).slice(0, 16).replace('T', ' ') : '')

Page({
  data: {
    id: null,
    dish: {},
    reviews: [],
    qty: 1,
    totalText: '0.00'
  },

  onLoad(options) {
    const id = Number(options.id)
    this.setData({ id })
    if (!id) {
      wx.showToast({ title: '菜品不存在', icon: 'none' })
      return
    }
    this.loadDish(id)
    this.loadReviews(id)
  },

  onShow() {
    api
      .cartCount()
      .then((count) => util.setCartBadge(count))
      .catch(() => {})
  },

  loadDish(id) {
    api
      .dishDetail(id)
      .then((dish) => {
        this.setData({
          dish: {
            ...dish,
            img: util.imageUrl(dish.image),
            priceText: util.money(dish.price),
            originalText:
              dish.originalPrice && Number(dish.originalPrice) > 0 ? util.money(dish.originalPrice) : ''
          }
        })
        this.updateTotal()
      })
      .catch(() => {})
  },

  loadReviews(id) {
    api
      .reviewsByDish(id)
      .then((list) => {
        const reviews = (list || []).map((review) => ({
          ...review,
          nickname: review.userNickname || '顾客',
          avatarText: (review.userNickname || '顾').slice(0, 1),
          stars: starsOf(review.rating),
          timeText: timeText(review.createTime),
          imagesList: util.parseImages(review.images).map(util.imageUrl)
        }))
        this.setData({ reviews })
      })
      .catch(() => {})
  },

  minus() {
    if (this.data.qty <= 1) return
    this.setData({ qty: this.data.qty - 1 })
    this.updateTotal()
  },

  plus() {
    if (this.data.qty >= 99) return
    this.setData({ qty: this.data.qty + 1 })
    this.updateTotal()
  },

  updateTotal() {
    const price = Number(this.data.dish.price || 0)
    this.setData({ totalText: util.money(price * this.data.qty) })
  },

  addToCart() {
    api
      .cartAdd(this.data.id, this.data.qty)
      .then(() => {
        wx.showToast({ title: '已加入购物车', icon: 'success' })
        return api.cartCount()
      })
      .then((count) => util.setCartBadge(count))
      .catch(() => {})
  }
})
