import request from '@/utils/request'

// 查询商品属性值(SKU)列表
export function listDiyStoreProductAttrValue(query) {
  return request({
    url: '/xms/diyStoreProductAttrValue/list',
    method: 'get',
    params: query
  })
}

// 查询商品属性值(SKU)详细
export function getDiyStoreProductAttrValue(id) {
  return request({
    url: '/xms/diyStoreProductAttrValue/' + id,
    method: 'get'
  })
}

// 新增商品属性值(SKU)
export function addDiyStoreProductAttrValue(data) {
  return request({
    url: '/xms/diyStoreProductAttrValue',
    method: 'post',
    data: data
  })
}

// 修改商品属性值(SKU)
export function updateDiyStoreProductAttrValue(data) {
  return request({
    url: '/xms/diyStoreProductAttrValue',
    method: 'put',
    data: data
  })
}

// 删除商品属性值(SKU)
export function delDiyStoreProductAttrValue(id) {
  return request({
    url: '/xms/diyStoreProductAttrValue/' + id,
    method: 'delete'
  })
}
