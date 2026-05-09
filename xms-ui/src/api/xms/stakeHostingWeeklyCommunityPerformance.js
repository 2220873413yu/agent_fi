import request from '@/utils/request'

// 查询托管每周新增小区业绩列表
export function listStakeHostingWeeklyCommunityPerformance(query) {
  return request({
    url: '/xms/stakeHostingWeeklyCommunityPerformance/list',
    method: 'get',
    params: query
  })
}

// 导出托管每周新增小区业绩
export function exportStakeHostingWeeklyCommunityPerformance(query) {
  return request({
    url: '/xms/stakeHostingWeeklyCommunityPerformance/export',
    method: 'post',
    params: query
  })
}
