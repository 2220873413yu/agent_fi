import request from '@/utils/request'

// 查询商品规则值列表
export function listDiyStoreProductRule(query) {
  return request({
    url: '/xms/diyStoreProductRule/list',
    method: 'get',
    params: query
  })
}

// 查询商品规则值详细
export function getDiyStoreProductRule(id) {
  return request({
    url: '/xms/diyStoreProductRule/' + id,
    method: 'get'
  })
}

// 新增商品规则值
export function addDiyStoreProductRule(data) {
  return request({
    url: '/xms/diyStoreProductRule',
    method: 'post',
    data: data
  })
}

// 修改商品规则值
export function updateDiyStoreProductRule(data) {
  return request({
    url: '/xms/diyStoreProductRule',
    method: 'put',
    data: data
  })
}

// 删除商品规则值
export function delDiyStoreProductRule(id) {
  return request({
    url: '/xms/diyStoreProductRule/' + id,
    method: 'delete'
  })
}
