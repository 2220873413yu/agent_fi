import request from '@/utils/request'

// 查询会员统计列表
export function getUserDataBoard(query) {
  return request({
    url: '/xms/dataBoard/getUserDataBoard',
    method: 'get',
    params: query
  })
}



// 查询手续费数据
export function listGroupTotal(query) {
  return request({
    url: '/xms/dataBoard/listGroupTotal',
    method: 'get',
    params: query
  })
}
  // 查询订单数据
export function listOrderGroupTotal(query) {
  return request({
    url: '/xms/dataBoard/listOrderGroupTotal',
    method: 'get',
    params: query
  })

}
