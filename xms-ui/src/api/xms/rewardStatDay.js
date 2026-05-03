import request from '@/utils/request'

// 查询每日奖励汇总列表
export function listRewardStatDay(query) {
  return request({
    url: '/xms/rewardStatDay/list',
    method: 'get',
    params: query
  })
}

// 查询每日奖励汇总详细
export function getRewardStatDay(id) {
  return request({
    url: '/xms/rewardStatDay/' + id,
    method: 'get'
  })
}

// 新增每日奖励汇总
export function addRewardStatDay(data) {
  return request({
    url: '/xms/rewardStatDay',
    method: 'post',
    data: data
  })
}

// 修改每日奖励汇总
export function updateRewardStatDay(data) {
  return request({
    url: '/xms/rewardStatDay',
    method: 'put',
    data: data
  })
}

// 删除每日奖励汇总
export function delRewardStatDay(id) {
  return request({
    url: '/xms/rewardStatDay/' + id,
    method: 'delete'
  })
}
