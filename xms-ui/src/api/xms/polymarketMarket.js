import request from '@/utils/request'

// 查询Polymarket市场聚合列表
export function listPolymarketMarket(query) {
  return request({
    url: '/xms/polymarketMarket/list',
    method: 'get',
    params: query
  })
}

// 查询Polymarket市场聚合详情
export function getPolymarketMarket(id) {
  return request({
    url: '/xms/polymarketMarket/' + id,
    method: 'get'
  })
}

// 手动触发单个Polymarket市场结算
export function settlePolymarketMarket(marketSlug) {
  return request({
    url: '/xms/polymarketMarket/settle/' + marketSlug,
    method: 'post'
  })
}
