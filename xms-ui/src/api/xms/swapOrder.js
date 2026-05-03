import request from '@/utils/request'

// 查询swap订单列表
export function listSwapOrder(query) {
  return request({
    url: '/xms/swapOrder/list',
    method: 'get',
    params: query
  })
}

// 查询swap订单详细
export function getSwapOrder(id) {
  return request({
    url: '/xms/swapOrder/' + id,
    method: 'get'
  })
}

// 新增swap订单
export function addSwapOrder(data) {
  return request({
    url: '/xms/swapOrder',
    method: 'post',
    data: data
  })
}

// 修改swap订单
export function updateSwapOrder(data) {
  return request({
    url: '/xms/swapOrder',
    method: 'put',
    data: data
  })
}

// 删除swap订单
export function delSwapOrder(id) {
  return request({
    url: '/xms/swapOrder/' + id,
    method: 'delete'
  })
}
