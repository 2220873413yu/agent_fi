import request from '@/utils/request'

// 查询节点取消订单归档列表
export function listNodePackageOrderCancel(query) {
  return request({
    url: '/xms/nodePackageOrderCancel/list',
    method: 'get',
    params: query
  })
}

// 查询节点取消订单归档详细
export function getNodePackageOrderCancel(id) {
  return request({
    url: '/xms/nodePackageOrderCancel/' + id,
    method: 'get'
  })
}
