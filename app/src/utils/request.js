/**
 * HTTP 请求封装（摇去哪后端对接）
 * - 统一注入 Authorization: Bearer token
 * - 401 时清理登录态并跳转登录页
 * - 业务失败（code!==200）统一 reject，message 抛出
 */

// 后端地址：公网通过 Nginx 反代（生产），本地调试可改回 http://localhost:8080
export const BASE_URL = 'http://180.76.105.198:8090'

let clearing = false
let refreshingPromise = null

function clearLogin() {
  if (clearing) return
  clearing = true
  try {
    uni.removeStorageSync('fw_token')
    uni.removeStorageSync('fw_user')
  } catch (e) { /* ignore */ }
  // 避免弹窗循环
  setTimeout(() => {
    clearing = false
    try {
      const pages = getCurrentPages()
      const cur = pages[pages.length - 1]
      if (cur && cur.route && !String(cur.route).includes('login')) {
        uni.reLaunch({ url: '/pages/login/login' })
      }
    } catch (e) { /* ignore */ }
  }, 300)
}

/**
 * 静默获取或刷新后端 JWT Token
 */
function obtainFreshToken() {
  if (refreshingPromise) return refreshingPromise
  refreshingPromise = new Promise((resolve) => {
    let openid = uni.getStorageSync('fw_openid')
    if (!openid) {
      openid = 'local_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
      uni.setStorageSync('fw_openid', openid)
    }
    uni.request({
      url: BASE_URL + '/api/login',
      method: 'POST',
      data: { openid, nickname: '微信用户', avatar: '' },
      header: { 'Content-Type': 'application/json' },
      timeout: 10000,
      success: (r) => {
        if (r.data && r.data.data && r.data.data.token) {
          const freshToken = r.data.data.token
          uni.setStorageSync('fw_token', freshToken)
          resolve(freshToken)
        } else {
          resolve(null)
        }
      },
      fail: () => resolve(null)
    })
  }).finally(() => {
    refreshingPromise = null
  })
  return refreshingPromise
}

export function request({ url, method = 'GET', data = {}, header = {} }) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('fw_token')
    const headers = { 'Content-Type': 'application/json', ...header }
    if (token) headers['Authorization'] = 'Bearer ' + token

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: headers,
      timeout: 15000,
      success: async (res) => {
        const body = res.data
        // 后端 AjaxResult: { code, msg, data }；成功 code === 200
        const isSuccess = (res.statusCode === 200 && body && (body.code === 200 || body.code === 0))
        const isAuthError = (res.statusCode === 401 || (body && (body.code === 401 || body.code === '401' || String(body.msg || '').includes('认证失败'))))

        if (isSuccess) {
          resolve(body)
        } else if (isAuthError && url !== '/api/login') {
          // 自动静默刷新 Token 并重试一次当前请求
          try {
            const freshToken = await obtainFreshToken()
            if (freshToken) {
              const retryHeaders = { ...headers, 'Authorization': 'Bearer ' + freshToken }
              uni.request({
                url: BASE_URL + url,
                method,
                data,
                header: retryHeaders,
                timeout: 15000,
                success: (retryRes) => {
                  const retryBody = retryRes.data
                  if (retryRes.statusCode === 200 && retryBody && (retryBody.code === 200 || retryBody.code === 0)) {
                    resolve(retryBody)
                  } else {
                    reject(new Error((retryBody && retryBody.msg) || '请求失败(' + retryRes.statusCode + ')'))
                  }
                },
                fail: (err) => reject(new Error('网络异常：' + (err.errMsg || '无法连接服务器')))
              })
              return
            }
          } catch (e) { /* ignore */ }
          clearLogin()
          reject(new Error('登录已过期，请重新登录'))
        } else {
          const msg = (body && body.msg) || ('请求失败(' + res.statusCode + ')')
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        // 后端未启动 / 网络不通
        reject(new Error('网络异常：' + (err.errMsg || '无法连接服务器')))
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })
