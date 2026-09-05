import request from '@/utils/request'

// 查询订单列表
export function listOrder(query) {
  return request({
    url: '/system/fworder/list',
    method: 'get',
    params: query
  })
}
