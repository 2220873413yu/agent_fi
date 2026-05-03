import request from '@/utils/request'

// 查询合同协议列表
export function listContract(query) {
  return request({
    url: '/xms/contract/list',
    method: 'get',
    params: query
  })
}

// 查询合同协议详细
export function getContract(id) {
  return request({
    url: '/xms/contract/' + id,
    method: 'get'
  })
}

// 新增合同协议
export function addContract(data) {
  return request({
    url: '/xms/contract',
    method: 'post',
    data: data
  })
}

// 修改合同协议
export function updateContract(data) {
  return request({
    url: '/xms/contract',
    method: 'put',
    data: data
  })
}

// 删除合同协议
export function delContract(id) {
  return request({
    url: '/xms/contract/' + id,
    method: 'delete'
  })
}
