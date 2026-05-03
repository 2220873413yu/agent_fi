import request from '@/utils/request'

// 查询appBanner图列表
export function listBanner(query) {
  return request({
    url: '/xms/banner/list',
    method: 'get',
    params: query
  })
}

// 查询appBanner图详细
export function getBanner(id) {
  return request({
    url: '/xms/banner/' + id,
    method: 'get'
  })
}

// 新增appBanner图
export function addBanner(data) {
  return request({
    url: '/xms/banner',
    method: 'post',
    data: data
  })
}

// 修改appBanner图
export function updateBanner(data) {
  return request({
    url: '/xms/banner',
    method: 'put',
    data: data
  })
}

// 删除appBanner图
export function delBanner(id) {
  return request({
    url: '/xms/banner/' + id,
    method: 'delete'
  })
}
