import request from '@/utils/request'

// 查询全平台托管累计金额列表
export function listStakeHostingUserAmountSummary(query) {
  return request({
    url: '/xms/stakeHostingUserAmountSummary/list',
    method: 'get',
    params: query
  })
}

// 手动调整全平台托管累计金额，正数增加，负数扣除
export function manualAdjustStakeHostingUserAmount(data) {
  return request({
    url: '/xms/stakeHostingUserAmountSummary/manualAdjust',
    method: 'put',
    data: data
  })
}

// 修改全平台托管累计金额备注
export function updateStakeHostingUserAmountRemark(data) {
  return request({
    url: '/xms/stakeHostingUserAmountSummary/remark',
    method: 'put',
    data: data
  })
}
