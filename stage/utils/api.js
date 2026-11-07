const { get, post, put, del } = require('./request')

module.exports = {
  categories: () => get('/api/category/list'),
  dishes: (params) => get('/api/dish/list', params),
  dishDetail: (id) => get('/api/dish/' + id),
  reviewsByDish: (id) => get('/api/review/dish/' + id),

  cartList: () => get('/api/cart/list'),
  cartAdd: (dishId, quantity) => post('/api/cart/add', { dishId, quantity: quantity || 1 }),
  cartUpdate: (dishId, quantity) => put('/api/cart/update', { dishId, quantity }),
  cartRemove: (dishId) => del('/api/cart/' + dishId),
  cartClear: () => del('/api/cart/clear'),
  cartCount: () => get('/api/cart/count'),

  tables: () => get('/api/table/list'),
  createOrder: (data) => post('/api/order/create', data),
  orders: (status) => get('/api/order/list', status === null || status === undefined ? {} : { status }),
  orderDetail: (id) => get('/api/order/' + id),
  payOrder: (id) => post('/api/order/' + id + '/pay'),
  cancelOrder: (id) => post('/api/order/' + id + '/cancel'),
  confirmOrder: (id) => post('/api/order/' + id + '/confirm'),

  addReview: (data) => post('/api/review/add', data),
  myReviews: () => get('/api/review/my'),

  userInfo: () => get('/api/wx/info'),
  updateProfile: (data) => put('/api/wx/profile', data)
}
