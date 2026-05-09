import request from '@/utils/request'

// 查询托管订单AFI质押记录列表
export function listStakeHostingAfiPledge(query) {
  return request({
    url: '/xms/stakeHostingAfiPledge/list',
    method: 'get',
    params: query
  })
}

// 查询托管订单AFI质押记录详细
export function getStakeHostingAfiPledge(id) {
  return request({
    url: '/xms/stakeHostingAfiPledge/' + id,
    method: 'get'
  })
}
