import request from '@/utils/request'

// 查询全球分红权重快照列表
export function listStakeHostingGlobalDividendWeightSnapshot(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendWeightSnapshot/list',
    method: 'get',
    params: query
  })
}
