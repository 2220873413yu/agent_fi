import request from '@/utils/request'

// 查询托管G7每日团队新增业绩与静态收益率快照列表
export function listStakeHostingDailyTeamPerformance(query) {
  return request({
    url: '/xms/stakeHostingDailyTeamPerformance/list',
    method: 'get',
    params: query
  })
}

// 查询托管G7每日团队新增业绩与静态收益率快照详情
export function getStakeHostingDailyTeamPerformance(id) {
  return request({
    url: '/xms/stakeHostingDailyTeamPerformance/' + id,
    method: 'get'
  })
}
