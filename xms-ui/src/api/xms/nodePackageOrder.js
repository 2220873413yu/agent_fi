import request from '@/utils/request'

// 查询节点购买记录列表
export function listNodePackageOrder(query) {
  return request({
    url: '/xms/nodePackageOrder/list',
    method: 'get',
    params: query
  })
}

// 查询节点购买记录详细
export function getNodePackageOrder(id) {
  return request({
    url: '/xms/nodePackageOrder/' + id,
    method: 'get'
  })
}

// 新增节点购买记录
export function addNodePackageOrder(data) {
  return request({
    url: '/xms/nodePackageOrder',
    method: 'post',
    data: data
  })
}

// 修改节点购买记录
export function updateNodePackageOrder(data) {
  return request({
    url: '/xms/nodePackageOrder',
    method: 'put',
    data: data
  })
}

// 删除节点购买记录
export function delNodePackageOrder(id) {
  return request({
    url: '/xms/nodePackageOrder/' + id,
    method: 'delete'
  })
}
