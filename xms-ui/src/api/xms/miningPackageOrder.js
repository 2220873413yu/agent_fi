import request from '@/utils/request'

// 查询矿机订单列表
export function listMiningPackageOrder(query) {
  return request({
    url: '/xms/miningPackageOrder/list',
    method: 'get',
    params: query
  })
}

// 查询矿机订单详细
export function getMiningPackageOrder(id) {
  return request({
    url: '/xms/miningPackageOrder/' + id,
    method: 'get'
  })
}

// 新增矿机订单
export function addMiningPackageOrder(data) {
  return request({
    url: '/xms/miningPackageOrder',
    method: 'post',
    data: data
  })
}

// 订单发货
export function handleProcessShipment(data) {
  return request({
    url: '/xms/miningPackageOrder/processShipment',
    method: 'post',
    data: data
  })
}

// 启用或者暂停矿机
export function handleStopOrOpenOrder(data) {
  return request({
    url: '/xms/miningPackageOrder/stopOrOpenOrder',
    method: 'post',
    data: data
  })
}

// 修改每日收益
export function handleUpdateDayReward(data) {
  return request({
    url: '/xms/miningPackageOrder/updateDayReward',
    method: 'post',
    data: data
  })
}

// 修改矿机订单
export function updateMiningPackageOrder(data) {
  return request({
    url: '/xms/miningPackageOrder',
    method: 'put',
    data: data
  })
}

// 删除矿机订单
export function delMiningPackageOrder(id) {
  return request({
    url: '/xms/miningPackageOrder/' + id,
    method: 'delete'
  })
}
