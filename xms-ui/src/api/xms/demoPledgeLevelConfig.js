import request from '@/utils/request'

// 查询示例质押等级配置列表
export function listDemoPledgeLevelConfig(query) {
  return request({
    url: '/xms/demoPledgeLevelConfig/list',
    method: 'get',
    params: query
  })
}

// 查询示例质押等级配置详细
export function getDemoPledgeLevelConfig(id) {
  return request({
    url: '/xms/demoPledgeLevelConfig/' + id,
    method: 'get'
  })
}

// 新增示例质押等级配置
export function addDemoPledgeLevelConfig(data) {
  return request({
    url: '/xms/demoPledgeLevelConfig',
    method: 'post',
    data: data
  })
}

// 修改示例质押等级配置
export function updateDemoPledgeLevelConfig(data) {
  return request({
    url: '/xms/demoPledgeLevelConfig',
    method: 'put',
    data: data
  })
}

// 删除示例质押等级配置
export function delDemoPledgeLevelConfig(id) {
  return request({
    url: '/xms/demoPledgeLevelConfig/' + id,
    method: 'delete'
  })
}
