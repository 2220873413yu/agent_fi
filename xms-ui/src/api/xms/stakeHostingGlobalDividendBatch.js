import request from '@/utils/request'

// 查询托管全球分红批次列表
export function listStakeHostingGlobalDividendBatch(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendBatch/list',
    method: 'get',
    params: query
  })
}
