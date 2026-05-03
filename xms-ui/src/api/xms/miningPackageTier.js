import request from '@/utils/request'

// 查询矿机质押区间配置列表
export function listMiningPackageTier(query) {
  return request({
    url: '/xms/miningPackageTier/list',
    method: 'get',
    params: query
  })
}

// 查询矿机质押区间配置详细
export function getMiningPackageTier(id) {
  return request({
    url: '/xms/miningPackageTier/' + id,
    method: 'get'
  })
}

// 新增矿机质押区间配置
export function addMiningPackageTier(data) {
  return request({
    url: '/xms/miningPackageTier',
    method: 'post',
    data: data
  })
}

// 修改矿机质押区间配置
export function updateMiningPackageTier(data) {
  return request({
    url: '/xms/miningPackageTier',
    method: 'put',
    data: data
  })
}

// 删除矿机质押区间配置
export function delMiningPackageTier(id) {
  return request({
    url: '/xms/miningPackageTier/' + id,
    method: 'delete'
  })
}
