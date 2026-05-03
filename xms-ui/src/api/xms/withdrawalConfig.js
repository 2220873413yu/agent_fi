import request from '@/utils/request'

// 查询提现配置列表
export function listWithdrawalConfig(query) {
  return request({
    url: '/xms/withdrawalConfig/list',
    method: 'get',
    params: query
  })
}

// 查询提现配置详细
export function getWithdrawalConfig(id) {
  return request({
    url: '/xms/withdrawalConfig/' + id,
    method: 'get'
  })
}

// 新增提现配置
export function addWithdrawalConfig(data) {
  return request({
    url: '/xms/withdrawalConfig',
    method: 'post',
    data: data
  })
}

// 修改提现配置
export function updateWithdrawalConfig(data) {
  return request({
    url: '/xms/withdrawalConfig',
    method: 'put',
    data: data
  })
}

// 删除提现配置
export function delWithdrawalConfig(id) {
  return request({
    url: '/xms/withdrawalConfig/' + id,
    method: 'delete'
  })
}
