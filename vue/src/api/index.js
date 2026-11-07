import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('wsx_admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      localStorage.removeItem('wsx_admin_token')
      localStorage.removeItem('wsx_admin_info')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    ElMessage.error(error.message || '网络异常，请检查后端服务')
    return Promise.reject(error)
  }
)

export const adminApi = {
  login: (data) => request.post('/admin/login', data),
  info: () => request.get('/admin/info'),
  dashboard: () => request.get('/admin/dashboard'),

  categories: () => request.get('/admin/category/list'),
  createCategory: (data) => request.post('/admin/category', data),
  updateCategory: (data) => request.put('/admin/category', data),
  deleteCategory: (id) => request.delete(`/admin/category/${id}`),

  dishes: (params) => request.get('/admin/dish/page', { params }),
  dishDetail: (id) => request.get(`/admin/dish/${id}`),
  createDish: (data) => request.post('/admin/dish', data),
  updateDish: (data) => request.put('/admin/dish', data),
  deleteDish: (id) => request.delete(`/admin/dish/${id}`),
  changeDishStatus: (id, status) => request.put(`/admin/dish/${id}/status`, null, { params: { status } }),

  orders: (params) => request.get('/admin/order/page', { params }),
  orderDetail: (id) => request.get(`/admin/order/${id}`),
  updateOrderStatus: (id, status) => request.put(`/admin/order/${id}/status`, null, { params: { status } }),

  reviews: (params) => request.get('/admin/review/page', { params }),
  replyReview: (id, reply) => request.put(`/admin/review/${id}/reply`, { reply }),
  deleteReview: (id) => request.delete(`/admin/review/${id}`),

  users: (params) => request.get('/admin/user/page', { params }),

  tables: () => request.get('/admin/table/list'),
  createTable: (data) => request.post('/admin/table', data),
  updateTable: (data) => request.put('/admin/table', data),
  deleteTable: (id) => request.delete(`/admin/table/${id}`)
}

export default request
