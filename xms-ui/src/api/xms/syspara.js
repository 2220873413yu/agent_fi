import request from '@/utils/request'

// 查询系统参数列表
export function listSyspara(query) {
  return request({
    url: '/xms/syspara/list',
    method: 'get',
    params: query
  })
}

// 查询系统参数详细
export function getSyspara(sysParaId) {
  return request({
    url: '/xms/syspara/' + sysParaId,
    method: 'get'
  })
}

// 新增系统参数
export function addSyspara(data) {
  return request({
    url: '/xms/syspara',
    method: 'post',
    data: data
  })
}

// 修改系统参数
export function updateSyspara(data) {
  return request({
    url: '/xms/syspara',
    method: 'put',
    data: data
  })
}

// 删除系统参数
export function delSyspara(sysParaId) {
  return request({
    url: '/xms/syspara/' + sysParaId,
    method: 'delete'
  })
}
