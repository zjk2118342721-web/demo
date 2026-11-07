const config = require('./utils/config')

App({
  globalData: {
    userInfo: null,
    loginPromise: null
  },

  onLaunch() {
    this.globalData.userInfo = wx.getStorageSync('wsx_user') || null
    this.ensureLogin().catch(() => {})
  },

  ensureLogin() {
    const token = wx.getStorageSync('wsx_token')
    if (token) {
      return Promise.resolve(token)
    }
    return this.login()
  },

  login() {
    if (this.globalData.loginPromise) {
      return this.globalData.loginPromise
    }
    this.globalData.loginPromise = new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          const code = config.MOCK_LOGIN ? config.MOCK_DEVICE_ID : res.code
          wx.request({
            url: config.BASE_URL + '/api/wx/login',
            method: 'POST',
            data: { code },
            header: { 'Content-Type': 'application/json' },
            timeout: 15000,
            success: (resp) => {
              const body = resp.data
              if (body && body.code === 200) {
                wx.setStorageSync('wsx_token', body.data.token)
                wx.setStorageSync('wsx_user', body.data.user)
                this.globalData.userInfo = body.data.user
                resolve(body.data.token)
              } else {
                reject(new Error((body && body.msg) || '登录失败'))
              }
            },
            fail: (err) => reject(err)
          })
        },
        fail: (err) => reject(err)
      })
    })
    return this.globalData.loginPromise
  },

  refreshLogin() {
    wx.removeStorageSync('wsx_token')
    this.globalData.loginPromise = null
    return this.login()
  }
})
