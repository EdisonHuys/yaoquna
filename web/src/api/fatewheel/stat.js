import request from '@/utils/request'

// 查询全局统计概览
export function getOverview() {
  return request({
    url: '/system/fwstat/overview',
    method: 'get'
  })
}
