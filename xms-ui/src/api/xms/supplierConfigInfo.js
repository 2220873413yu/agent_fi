import request from '@/utils/request'

// 查询供应商配置信息列表
export function listSupplierConfigInfo(query) {
  return request({
    url: '/xms/supplierConfigInfo/list',
    method: 'get',
    params: query
  })
}

// 查询供应商配置信息详细
export function getSupplierConfigInfo(id) {
  return request({
    url: '/xms/supplierConfigInfo/' + id,
    method: 'get'
  })
}

// 新增供应商配置信息
export function addSupplierConfigInfo(data) {
  return request({
    url: '/xms/supplierConfigInfo',
    method: 'post',
    data: data
  })
}

// 修改供应商配置信息
export function updateSupplierConfigInfo(data) {
  return request({
    url: '/xms/supplierConfigInfo',
    method: 'put',
    data: data
  })
}

// 删除供应商配置信息
export function delSupplierConfigInfo(id) {
  return request({
    url: '/xms/supplierConfigInfo/' + id,
    method: 'delete'
  })
}
