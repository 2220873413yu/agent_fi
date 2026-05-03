import request from '@/utils/request'

// 查询用户钱包列表
export function listUsermoney(query) {
  return request({
    url: '/xms/usermoney/list',
    method: 'get',
    params: query
  })
}

// 查询用户钱包详细
export function getUsermoney(id) {
  return request({
    url: '/xms/usermoney/' + id,
    method: 'get'
  })
}

// 新增用户钱包
export function addUsermoney(data) {
  return request({
    url: '/xms/usermoney',
    method: 'post',
    data: data
  })
}

// 修改用户钱包
export function updateUsermoney(data) {
  return request({
    url: '/xms/usermoney',
    method: 'put',
    data: data
  })
}

// 删除用户钱包
export function delUsermoney(id) {
  return request({
    url: '/xms/usermoney/' + id,
    method: 'delete'
  })
}