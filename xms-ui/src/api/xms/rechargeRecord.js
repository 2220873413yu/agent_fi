import request from '@/utils/request'

// 查询充值记录列表
export function listRechargeRecord(query) {
  return request({
    url: '/xms/rechargeRecord/list',
    method: 'get',
    params: query
  })
}

// 查询充值记录详细
export function getRechargeRecord(id) {
  return request({
    url: '/xms/rechargeRecord/' + id,
    method: 'get'
  })
}

// 新增充值记录
export function addRechargeRecord(data) {
  return request({
    url: '/xms/rechargeRecord',
    method: 'post',
    data: data
  })
}

// 修改充值记录
export function updateRechargeRecord(data) {
  return request({
    url: '/xms/rechargeRecord',
    method: 'put',
    data: data
  })
}

// 删除充值记录
export function delRechargeRecord(id) {
  return request({
    url: '/xms/rechargeRecord/' + id,
    method: 'delete'
  })
}
