import request from '@/utils/request'

// 查询节点线性释放订单列表
export function listNodePackageReleaseOrder(query) {
  return request({
    url: '/xms/nodePackageReleaseOrder/list',
    method: 'get',
    params: query
  })
}

// 查询节点线性释放订单详细
export function getNodePackageReleaseOrder(id) {
  return request({
    url: '/xms/nodePackageReleaseOrder/' + id,
    method: 'get'
  })
}
