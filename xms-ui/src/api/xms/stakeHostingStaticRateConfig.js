import request from '@/utils/request'

// 查询G7静态收益率配置列表
export function listStakeHostingStaticRateConfig(query) {
  return request({
    url: '/xms/stakeHostingStaticRateConfig/list',
    method: 'get',
    params: query
  })
}

// 查询G7静态收益率配置详细
export function getStakeHostingStaticRateConfig(id) {
  return request({
    url: '/xms/stakeHostingStaticRateConfig/' + id,
    method: 'get'
  })
}

// 新增G7静态收益率配置
export function addStakeHostingStaticRateConfig(data) {
  return request({
    url: '/xms/stakeHostingStaticRateConfig',
    method: 'post',
    data: data
  })
}

// 修改G7静态收益率配置
export function updateStakeHostingStaticRateConfig(data) {
  return request({
    url: '/xms/stakeHostingStaticRateConfig',
    method: 'put',
    data: data
  })
}

// 删除G7静态收益率配置
export function delStakeHostingStaticRateConfig(id) {
  return request({
    url: '/xms/stakeHostingStaticRateConfig/' + id,
    method: 'delete'
  })
}
