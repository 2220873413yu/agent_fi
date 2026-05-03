import request from '@/utils/request'

// 查询版本列表
export function listSysversionInfo(query) {
  return request({
    url: '/xms/sysversionInfo/list',
    method: 'get',
    params: query
  })
}

// 查询版本详细
export function getSysversionInfo(id) {
  return request({
    url: '/xms/sysversionInfo/' + id,
    method: 'get'
  })
}

// 新增版本
export function addSysversionInfo(data) {
  return request({
    url: '/xms/sysversionInfo',
    method: 'post',
    data: data
  })
}

// 修改版本
export function updateSysversionInfo(data) {
  return request({
    url: '/xms/sysversionInfo',
    method: 'put',
    data: data
  })
}

// 删除版本
export function delSysversionInfo(id) {
  return request({
    url: '/xms/sysversionInfo/' + id,
    method: 'delete'
  })
}
