import request from '@/utils/request'

// 查询矿机奖励分配配置列表
export function listMiningRewardConfig(query) {
  return request({
    url: '/xms/miningRewardConfig/list',
    method: 'get',
    params: query
  })
}

// 查询矿机奖励分配配置详细
export function getMiningRewardConfig(id) {
  return request({
    url: '/xms/miningRewardConfig/' + id,
    method: 'get'
  })
}

// 新增矿机奖励分配配置
export function addMiningRewardConfig(data) {
  return request({
    url: '/xms/miningRewardConfig',
    method: 'post',
    data: data
  })
}

// 修改矿机奖励分配配置
export function updateMiningRewardConfig(data) {
  return request({
    url: '/xms/miningRewardConfig',
    method: 'put',
    data: data
  })
}

// 删除矿机奖励分配配置
export function delMiningRewardConfig(id) {
  return request({
    url: '/xms/miningRewardConfig/' + id,
    method: 'delete'
  })
}
