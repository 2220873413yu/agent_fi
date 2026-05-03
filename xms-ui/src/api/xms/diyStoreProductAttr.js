import request from '@/utils/request'

// 查询商品属性列表
export function listDiyStoreProductAttr(query) {
  return request({
    url: '/xms/diyStoreProductAttr/list',
    method: 'get',
    params: query
  })
}

// 查询商品属性详细
export function getDiyStoreProductAttr(id) {
  return request({
    url: '/xms/diyStoreProductAttr/' + id,
    method: 'get'
  })
}

// 新增商品属性
export function addDiyStoreProductAttr(data) {
  return request({
    url: '/xms/diyStoreProductAttr',
    method: 'post',
    data: data
  })
}

// 修改商品属性
export function updateDiyStoreProductAttr(data) {
  return request({
    url: '/xms/diyStoreProductAttr',
    method: 'put',
    data: data
  })
}

// 删除商品属性
export function delDiyStoreProductAttr(id) {
  return request({
    url: '/xms/diyStoreProductAttr/' + id,
    method: 'delete'
  })
}
