import request from '@/utils/request'

// 查询用户等级考核配置列表
export function listUserLevelConfig(query) {
  return request({
    url: '/xms/userLevelConfig/list',
    method: 'get',
    params: query
  })
}

// 查询用户等级考核配置详细
export function getUserLevelConfig(id) {
  return request({
    url: '/xms/userLevelConfig/' + id,
    method: 'get'
  })
}

// 新增用户等级考核配置
export function addUserLevelConfig(data) {
  return request({
    url: '/xms/userLevelConfig',
    method: 'post',
    data: data
  })
}

// 修改用户等级考核配置
export function updateUserLevelConfig(data) {
  return request({
    url: '/xms/userLevelConfig',
    method: 'put',
    data: data
  })
}

// 删除用户等级考核配置
export function delUserLevelConfig(id) {
  return request({
    url: '/xms/userLevelConfig/' + id,
    method: 'delete'
  })
}
