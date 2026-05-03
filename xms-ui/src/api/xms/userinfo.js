import request from '@/utils/request'

// 查询用户信息列表
export function listUserinfo(query) {
  return request({
    url: '/xms/userinfo/list',
    method: 'get',
    params: query
  })
}

// 查询下级团队用户信息列表
export function childListUserinfo(query) {
  return request({
    url: '/xms/userinfo/childList',
    method: 'get',
    params: query
  })
}

// 查询用户信息详细
export function getUserinfo(userId) {
  return request({
    url: '/xms/userinfo/' + userId,
    method: 'get'
  })
}

// 查询直推用户信息列表
export function subUserList(query) {
  return request({
    url: '/xms/userinfo/subUserList',
    method: 'get',
    params: query
  })
}

// 新增用户信息
export function addUserinfo(data) {
  return request({
    url: '/xms/userinfo',
    method: 'post',
    data: data
  })
}

// 修改用户信息
export function updateUserinfo(data) {
  return request({
    url: '/xms/userinfo',
    method: 'put',
    data: data
  })
}

// 删除用户信息
export function delUserinfo(userId) {
  return request({
    url: '/xms/userinfo/' + userId,
    method: 'delete'
  })
}


// 查询网体关系-树结构 userId实际意义是用户编码
export function queryNetBody1(userId) {
   return request({
    url: '/xms/userinfo/queryNetBody1',
    method: 'get',
    params: {
      userId: userId
    }
  })
}

// 关闭团队挖矿
export function closeTeamMining(userId) {
  return request({
    url: '/xms/userinfo/closeTeamMining',
    method: 'get',
    params: { userId }
  })
}

// 开启团队挖矿
export function changeGoogleCode(userId) {
  return request({
    url: '/xms/userinfo/changeGoogleCode',
    method: 'get',
    params: { userId }
  })
}

// 开启或者关闭团队提现
export function offTeamWithdrawal(userId,bizType) {
  return request({
    url: '/xms/userinfo/offTeamWithdrawal',
    method: 'get',
    params: { userId, bizType }
  })
}
