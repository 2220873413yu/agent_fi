import request from '@/utils/request'

// 查询矿机管理费配置列表
export function listMiningMgmtFeeConfig(query) {
  return request({
    url: '/xms/miningMgmtFeeConfig/list',
    method: 'get',
    params: query
  })
}

// 查询矿机管理费配置详细
export function getMiningMgmtFeeConfig(id) {
  return request({
    url: '/xms/miningMgmtFeeConfig/' + id,
    method: 'get'
  })
}

// 新增矿机管理费配置
export function addMiningMgmtFeeConfig(data) {
  return request({
    url: '/xms/miningMgmtFeeConfig',
    method: 'post',
    data: data
  })
}

// 修改矿机管理费配置
export function updateMiningMgmtFeeConfig(data) {
  return request({
    url: '/xms/miningMgmtFeeConfig',
    method: 'put',
    data: data
  })
}

// 删除矿机管理费配置
export function delMiningMgmtFeeConfig(id) {
  return request({
    url: '/xms/miningMgmtFeeConfig/' + id,
    method: 'delete'
  })
}
