import request from '@/utils/request'

// 查询学校列表
export function listSchool(query) {
  return request({
    url: '/system/fwschool/list',
    method: 'get',
    params: query
  })
}

// 查询学校详细
export function getSchool(schoolId) {
  return request({
    url: '/system/fwschool/' + schoolId,
    method: 'get'
  })
}

// 新增学校
export function addSchool(data) {
  return request({
    url: '/system/fwschool',
    method: 'post',
    data: data
  })
}

// 修改学校
export function updateSchool(data) {
  return request({
    url: '/system/fwschool',
    method: 'put',
    data: data
  })
}

// 删除学校
export function delSchool(schoolIds) {
  return request({
    url: '/system/fwschool/' + schoolIds,
    method: 'delete'
  })
}
