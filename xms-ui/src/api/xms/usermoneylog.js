import request from '@/utils/request'

// 查询钱包流水列表
export function listUsermoneylog(query) {
  return request({
    url: '/xms/usermoneylog/list',
    method: 'get',
    params: query
  })
}

// 查询钱包流水详细
export function getUsermoneylog(id) {
  return request({
    url: '/xms/usermoneylog/' + id,
    method: 'get'
  })
}

// 新增钱包流水
export function addUsermoneylog(data) {
  return request({
    url: '/xms/usermoneylog',
    method: 'post',
    data: data
  })
}

// 修改钱包流水
export function updateUsermoneylog(data) {
  return request({
    url: '/xms/usermoneylog',
    method: 'put',
    data: data
  })
}

// 删除钱包流水
export function delUsermoneylog(id) {
  return request({
    url: '/xms/usermoneylog/' + id,
    method: 'delete'
  })
}
