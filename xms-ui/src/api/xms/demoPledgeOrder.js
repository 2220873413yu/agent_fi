import request from '@/utils/request'

// 查询示例质押订单列表
export function listDemoPledgeOrder(query) {
  return request({
    url: '/xms/demoPledgeOrder/list',
    method: 'get',
    params: query
  })
}

// 查询示例质押订单详细
export function getDemoPledgeOrder(id) {
  return request({
    url: '/xms/demoPledgeOrder/' + id,
    method: 'get'
  })
}

// 后台演示购买示例质押套餐
export function buyDemoPledgeOrder(data) {
  return request({
    url: '/xms/demoPledgeOrder/buy',
    method: 'post',
    data: data
  })
}
