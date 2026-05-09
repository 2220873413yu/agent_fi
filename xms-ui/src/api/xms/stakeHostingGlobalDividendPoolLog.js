import request from '@/utils/request'

// 查询托管全球分红奖池流水列表
export function listStakeHostingGlobalDividendPoolLog(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendPoolLog/list',
    method: 'get',
    params: query
  })
}
