import request from '@/utils/request'

// 查询托管套餐列表
export function listStakeHostingPackage(query) {
  return request({
    url: '/xms/stakeHostingPackage/list',
    method: 'get',
    params: query
  })
}

// 查询托管套餐详细
export function getStakeHostingPackage(id) {
  return request({
    url: '/xms/stakeHostingPackage/' + id,
    method: 'get'
  })
}

// 新增托管套餐
export function addStakeHostingPackage(data) {
  return request({
    url: '/xms/stakeHostingPackage',
    method: 'post',
    data: data
  })
}

// 修改托管套餐
export function updateStakeHostingPackage(data) {
  return request({
    url: '/xms/stakeHostingPackage',
    method: 'put',
    data: data
  })
}

// 删除托管套餐
export function delStakeHostingPackage(id) {
  return request({
    url: '/xms/stakeHostingPackage/' + id,
    method: 'delete'
  })
}
