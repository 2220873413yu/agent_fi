import request from '@/utils/request'

// 查询谷歌邮箱配置列表
export function listEmailConfig(query) {
  return request({
    url: '/xms/emailConfig/list',
    method: 'get',
    params: query
  })
}

// 查询谷歌邮箱配置详细
export function getEmailConfig(id) {
  return request({
    url: '/xms/emailConfig/' + id,
    method: 'get'
  })
}

// 新增谷歌邮箱配置
export function addEmailConfig(data) {
  return request({
    url: '/xms/emailConfig',
    method: 'post',
    data: data
  })
}

// 修改谷歌邮箱配置
export function updateEmailConfig(data) {
  return request({
    url: '/xms/emailConfig',
    method: 'put',
    data: data
  })
}

// 删除谷歌邮箱配置
export function delEmailConfig(id) {
  return request({
    url: '/xms/emailConfig/' + id,
    method: 'delete'
  })
}
