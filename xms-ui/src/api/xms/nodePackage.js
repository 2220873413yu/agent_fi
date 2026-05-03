import request from '@/utils/request'

// 查询节点套餐列表
export function listNodePackage(query) {
  return request({
    url: '/xms/nodePackage/list',
    method: 'get',
    params: query
  })
}

// 查询节点套餐详细
export function getNodePackage(id) {
  return request({
    url: '/xms/nodePackage/' + id,
    method: 'get'
  })
}

// 新增节点套餐
export function addNodePackage(data) {
  return request({
    url: '/xms/nodePackage',
    method: 'post',
    data: data
  })
}

// 修改节点套餐
export function updateNodePackage(data) {
  return request({
    url: '/xms/nodePackage',
    method: 'put',
    data: data
  })
}

// 删除节点套餐
export function delNodePackage(id) {
  return request({
    url: '/xms/nodePackage/' + id,
    method: 'delete'
  })
}
