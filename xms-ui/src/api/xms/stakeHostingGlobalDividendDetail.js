import request from '@/utils/request'

// 查询托管全球分红明细列表
export function listStakeHostingGlobalDividendDetail(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendDetail/list',
    method: 'get',
    params: query
  })
}
