import request from '@/utils/request'

// 查询商品列表
export function listDiyStoreProduct(query) {
  return request({
    url: '/xms/diyStoreProduct/list',
    method: 'get',
    params: query
  })
}

// 查询商品可选规格模板列表
export function getProductRuleList() {
  return request({
    url: '/xms/diyStoreProduct/getProductRuleList',
    method: 'get'
  })
}

// 查询商品详细
export function getDiyStoreProduct(id) {
  return request({
    url: '/xms/diyStoreProduct/' + id,
    method: 'get'
  })
}

// 新增商品
export function addDiyStoreProduct(data) {
  return request({
    url: '/xms/diyStoreProduct',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateDiyStoreProduct(data) {
  return request({
    url: '/xms/diyStoreProduct',
    method: 'put',
    data: data
  })
}

// 删除商品
export function delDiyStoreProduct(id) {
  return request({
    url: '/xms/diyStoreProduct/' + id,
    method: 'delete'
  })
}
