import request from '@/utils/request'

// 查询用户扩展列表
export function listUserExtend(query) {
  return request({
    url: '/system/fwuserext/list',
    method: 'get',
    params: query
  })
}
