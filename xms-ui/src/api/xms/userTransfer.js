import request from '@/utils/request'

// 查询用户转账记录列表
export function listUserTransfer(query) {
  return request({
    url: '/xms/userTransfer/list',
    method: 'get',
    params: query
  })
}

// 查询用户转账记录详细
export function getUserTransfer(id) {
  return request({
    url: '/xms/userTransfer/' + id,
    method: 'get'
  })
}

// 新增用户转账记录
export function addUserTransfer(data) {
  return request({
    url: '/xms/userTransfer',
    method: 'post',
    data: data
  })
}

// 修改用户转账记录
export function updateUserTransfer(data) {
  return request({
    url: '/xms/userTransfer',
    method: 'put',
    data: data
  })
}

// 删除用户转账记录
export function delUserTransfer(id) {
  return request({
    url: '/xms/userTransfer/' + id,
    method: 'delete'
  })
}
