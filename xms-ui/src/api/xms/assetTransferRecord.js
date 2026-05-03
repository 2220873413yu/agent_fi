import request from '@/utils/request'

// 查询DF划转记录列表
export function listAssetTransferRecord(query) {
  return request({
    url: '/xms/assetTransferRecord/list',
    method: 'get',
    params: query
  })
}

// 查询DF划转记录详细
export function getAssetTransferRecord(id) {
  return request({
    url: '/xms/assetTransferRecord/' + id,
    method: 'get'
  })
}

// 新增DF划转记录
export function addAssetTransferRecord(data) {
  return request({
    url: '/xms/assetTransferRecord',
    method: 'post',
    data: data
  })
}

// 修改DF划转记录
export function updateAssetTransferRecord(data) {
  return request({
    url: '/xms/assetTransferRecord',
    method: 'put',
    data: data
  })
}

// 删除DF划转记录
export function delAssetTransferRecord(id) {
  return request({
    url: '/xms/assetTransferRecord/' + id,
    method: 'delete'
  })
}
