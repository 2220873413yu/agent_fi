import request from '@/utils/request'

// 查询托管订单列表
export function listStakeHostingOrder(query) {
  return request({
    url: '/xms/stakeHostingOrder/list',
    method: 'get',
    params: query
  })
}

// 查询托管订单详细
export function getStakeHostingOrder(id) {
  return request({
    url: '/xms/stakeHostingOrder/' + id,
    method: 'get'
  })
}

// 后台拨付托管订单
export function addStakeHostingOrder(data) {
  return request({
    url: '/xms/stakeHostingOrder',
    method: 'post',
    data: data
  })
}

// 修改托管订单
export function updateStakeHostingOrder(data) {
  return request({
    url: '/xms/stakeHostingOrder',
    method: 'put',
    data: data
  })
}

// 删除托管订单
export function delStakeHostingOrder(id) {
  return request({
    url: '/xms/stakeHostingOrder/' + id,
    method: 'delete'
  })
}
