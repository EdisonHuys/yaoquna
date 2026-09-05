import request from '@/utils/request'

// 查询举报列表
export function listReport(query) {
  return request({
    url: '/system/fwreport/list',
    method: 'get',
    params: query
  })
}

// 处理举报
export function handleReport(data) {
  return request({
    url: '/system/fwreport',
    method: 'put',
    data: data
  })
}
