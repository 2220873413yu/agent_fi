import request from '@/utils/request'

// 查询托管全球分红奖池
export function getStakeHostingGlobalDividendPool() {
  return request({
    url: '/xms/stakeHostingGlobalDividendPool/info',
    method: 'get'
  })
}

// 手动调增/调减托管全球分红奖池
export function adjustStakeHostingGlobalDividendPool(data) {
  return request({
    url: '/xms/stakeHostingGlobalDividendPool/adjust',
    method: 'post',
    data: data
  })
}
