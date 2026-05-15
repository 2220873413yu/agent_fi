import request from '@/utils/request'

// 查询托管全球分红权重快照列表
export function listStakeHostingGlobalDividendWeightSnapshot(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendWeightSnapshot/list',
    method: 'get',
    params: query
  })
}

// 导出托管全球分红权重快照
export function exportStakeHostingGlobalDividendWeightSnapshot(query) {
  return request({
    url: '/xms/stakeHostingGlobalDividendWeightSnapshot/export',
    method: 'post',
    params: query
  })
}
