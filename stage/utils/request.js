const { BASE_URL } = require('./config')

function request(options) {
  const { url, method = 'GET', data = {}, auth = true } = options
  return new Promise((resolve, reject) => {
    const app = getApp()

    const doRequest = () => {
      const token = wx.getStorageSync('wsx_token')
      const header = { 'Content-Type': 'application/json' }
      if (auth && token) {
        header.Authorization = 'Bearer ' + token
      }
      wx.request({
        url: BASE_URL + url,
        method,
        data,
        header,
        timeout: 15000,
        success: (res) => {
          const body = res.data
          if (body && body.code === 200) {
            resolve(body.data)
            return
          }
          if (body && body.code === 401 && auth && options.retry !== false) {
            app
              .refreshLogin()
              .then(() => request(Object.assign({}, options, { retry: false })))
              .then(resolve)
              .catch(reject)
            return
          }
          wx.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' })
          reject(body)
        },
        fail: (err) => {
          wx.showToast({ title: '网络异常，请检查后端服务', icon: 'none' })
          reject(err)
        }
      })
    }

    if (auth && app) {
      app
        .ensureLogin()
        .then(doRequest)
        .catch(doRequest)
    } else {
      doRequest()
    }
  })
}

const get = (url, data) => request({ url, method: 'GET', data })
const post = (url, data) => request({ url, method: 'POST', data })
const put = (url, data) => request({ url, method: 'PUT', data })
const del = (url, data) => request({ url, method: 'DELETE', data })

module.exports = { request, get, post, put, del }
