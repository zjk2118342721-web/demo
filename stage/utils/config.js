// 后端服务地址：真机调试时请改为电脑的局域网 IP，例如 http://192.168.1.10:8080
const BASE_URL = 'http://localhost:8080'

module.exports = {
  BASE_URL,
  // 本地演示登录：使用稳定的模拟设备标识，对应数据库中预置的演示顾客
  // 配置真实微信 AppID/Secret 后，将 MOCK_LOGIN 改为 false
  MOCK_LOGIN: true,
  MOCK_DEVICE_ID: 'demo'
}
