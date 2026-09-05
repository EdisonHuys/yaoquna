import request from '@/utils/request'

// 查询标记列表
export function listMark(query) {
  return request({
    url: '/system/fwmark/list',
    method: 'get',
    params: query
  })
}

// 查询标记详细
export function getMark(markId) {
  return request({
    url: '/system/fwmark/' + markId,
    method: 'get'
  })
}

// 新增标记
export function addMark(data) {
  return request({
    url: '/system/fwmark',
    method: 'post',
    data: data
  })
}

// 修改标记
export function updateMark(data) {
  return request({
    url: '/system/fwmark',
    method: 'put',
    data: data
  })
}

// 删除标记
export function delMark(markIds) {
  return request({
    url: '/system/fwmark/' + markIds,
    method: 'delete'
  })
}
