import request from '@/utils/request'

// 查询质押收益线性释放列表
export function listStakeReleaseBucket(query) {
  return request({
    url: '/xms/stakeReleaseBucket/list',
    method: 'get',
    params: query
  })
}

// 查询质押收益线性释放详细
export function getStakeReleaseBucket(id) {
  return request({
    url: '/xms/stakeReleaseBucket/' + id,
    method: 'get'
  })
}

// 新增质押收益线性释放
export function addStakeReleaseBucket(data) {
  return request({
    url: '/xms/stakeReleaseBucket',
    method: 'post',
    data: data
  })
}

// 修改质押收益线性释放
export function updateStakeReleaseBucket(data) {
  return request({
    url: '/xms/stakeReleaseBucket',
    method: 'put',
    data: data
  })
}

// 删除质押收益线性释放
export function delStakeReleaseBucket(id) {
  return request({
    url: '/xms/stakeReleaseBucket/' + id,
    method: 'delete'
  })
}
