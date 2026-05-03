import request from '@/utils/request'

// 查询收货地址列表
export function listUserAddress(query) {
  return request({
    url: '/xms/userAddress/list',
    method: 'get',
    params: query
  })
}

// 查询收货地址详细
export function getUserAddress(id) {
  return request({
    url: '/xms/userAddress/' + id,
    method: 'get'
  })
}

// 新增收货地址
export function addUserAddress(data) {
  return request({
    url: '/xms/userAddress',
    method: 'post',
    data: data
  })
}

// 修改收货地址
export function updateUserAddress(data) {
  return request({
    url: '/xms/userAddress',
    method: 'put',
    data: data
  })
}

// 删除收货地址
export function delUserAddress(id) {
  return request({
    url: '/xms/userAddress/' + id,
    method: 'delete'
  })
}
