import request from '@/utils/request'

// 查询托管奖励结算明细列表
export function listStakeHostingRewardSettlement(query) {
  return request({
    url: '/xms/stakeHostingRewardSettlement/list',
    method: 'get',
    params: query
  })
}
