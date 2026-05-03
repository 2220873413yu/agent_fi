import request from '@/utils/request'

// 查询质押订单列表
export function listStakeOrder(query) {
  return request({
    url: '/xms/stakeOrder/list',
    method: 'get',
    params: query
  })
}

// 查询质押订单详细
export function getStakeOrder(id) {
  return request({
    url: '/xms/stakeOrder/' + id,
    method: 'get'
  })
}

// 新增质押订单
export function addStakeOrder(data) {
  return request({
    url: '/xms/stakeOrder',
    method: 'post',
    data: data
  })
}

// 下架质押订单
export function handleDisableStakeOrder(data) {
  return request({
    url: '/xms/stakeOrder/disableStakeOrder',
    method: 'post',
    data: data
  })
}

// 质押订单发货
export function handleOrderShipped(data) {
  return request({
    url: '/xms/stakeOrder/orderShipped',
    method: 'post',
    data: data
  })
}

// 修改质押订单
export function updateStakeOrder(data) {
  return request({
    url: '/xms/stakeOrder',
    method: 'put',
    data: data
  })
}

// 删除质押订单
export function delStakeOrder(id) {
  return request({
    url: '/xms/stakeOrder/' + id,
    method: 'delete'
  })
}
