import request from '@/utils/request'

// 查询奖金记录列表
export function listRewardRecord(query) {
  return request({
    url: '/xms/rewardRecord/list',
    method: 'get',
    params: query
  })
}

// 查询奖金记录详细
export function getRewardRecord(id) {
  return request({
    url: '/xms/rewardRecord/' + id,
    method: 'get'
  })
}

// 新增奖金记录
export function addRewardRecord(data) {
  return request({
    url: '/xms/rewardRecord',
    method: 'post',
    data: data
  })
}

// 修改奖金记录
export function updateRewardRecord(data) {
  return request({
    url: '/xms/rewardRecord',
    method: 'put',
    data: data
  })
}

// 删除奖金记录
export function delRewardRecord(id) {
  return request({
    url: '/xms/rewardRecord/' + id,
    method: 'delete'
  })
}
