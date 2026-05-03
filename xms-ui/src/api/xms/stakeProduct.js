import request from '@/utils/request'

// 查询质押套餐列表
export function listStakeProduct(query) {
  return request({
    url: '/xms/stakeProduct/list',
    method: 'get',
    params: query
  })
}

// 查询质押套餐详细
export function getStakeProduct(id) {
  return request({
    url: '/xms/stakeProduct/' + id,
    method: 'get'
  })
}

// 新增质押套餐
export function addStakeProduct(data) {
  return request({
    url: '/xms/stakeProduct',
    method: 'post',
    data: data
  })
}

// 修改质押套餐
export function updateStakeProduct(data) {
  return request({
    url: '/xms/stakeProduct',
    method: 'put',
    data: data
  })
}

// 删除质押套餐
export function delStakeProduct(id) {
  return request({
    url: '/xms/stakeProduct/' + id,
    method: 'delete'
  })
}
