import request from '@/utils/request'

// 查询AFI质押加速配置列表
export function listStakeHostingAfiAccelerateConfig(query) {
  return request({
    url: '/xms/stakeHostingAfiAccelerateConfig/list',
    method: 'get',
    params: query
  })
}

// 查询AFI质押加速配置详细
export function getStakeHostingAfiAccelerateConfig(id) {
  return request({
    url: '/xms/stakeHostingAfiAccelerateConfig/' + id,
    method: 'get'
  })
}

// 新增AFI质押加速配置
export function addStakeHostingAfiAccelerateConfig(data) {
  return request({
    url: '/xms/stakeHostingAfiAccelerateConfig',
    method: 'post',
    data: data
  })
}

// 修改AFI质押加速配置
export function updateStakeHostingAfiAccelerateConfig(data) {
  return request({
    url: '/xms/stakeHostingAfiAccelerateConfig',
    method: 'put',
    data: data
  })
}

// 删除AFI质押加速配置
export function delStakeHostingAfiAccelerateConfig(id) {
  return request({
    url: '/xms/stakeHostingAfiAccelerateConfig/' + id,
    method: 'delete'
  })
}
