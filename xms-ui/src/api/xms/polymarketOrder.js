import request from '@/utils/request'

// 查询Polymarket内部订单列表
export function listPolymarketOrder(query) {
  return request({
    url: '/xms/polymarketOrder/list',
    method: 'get',
    params: query
  })
}

// 查询Polymarket内部订单详情
export function getPolymarketOrder(id) {
  return request({
    url: '/xms/polymarketOrder/' + id,
    method: 'get'
  })
}

// 修改Polymarket内部订单复核信息
export function updatePolymarketOrder(data) {
  return request({
    url: '/xms/polymarketOrder',
    method: 'put',
    data: data
  })
}

// 触发Polymarket待结算订单处理
export function settlePendingPolymarketOrder(limit) {
  return request({
    url: '/xms/polymarketOrder/settlePending',
    method: 'post',
    params: { limit }
  })
}
