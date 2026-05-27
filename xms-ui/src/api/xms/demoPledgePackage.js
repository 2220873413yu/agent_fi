import request from '@/utils/request'

// 查询示例质押套餐列表
export function listDemoPledgePackage(query) {
  return request({
    url: '/xms/demoPledgePackage/list',
    method: 'get',
    params: query
  })
}

// 查询示例质押套餐详细
export function getDemoPledgePackage(id) {
  return request({
    url: '/xms/demoPledgePackage/' + id,
    method: 'get'
  })
}

// 新增示例质押套餐
export function addDemoPledgePackage(data) {
  return request({
    url: '/xms/demoPledgePackage',
    method: 'post',
    data: data
  })
}

// 修改示例质押套餐
export function updateDemoPledgePackage(data) {
  return request({
    url: '/xms/demoPledgePackage',
    method: 'put',
    data: data
  })
}

// 删除示例质押套餐
export function delDemoPledgePackage(id) {
  return request({
    url: '/xms/demoPledgePackage/' + id,
    method: 'delete'
  })
}
