package com.xms.app.entity.req;

import lombok.Data;

import java.util.List;

/**
 * 查询奖金记录的请求参数
 * @author: hk
 * @description:
 * @date: 2020-05-05 17:05
 */
@Data
   public class RewardRecordQueryReq {
       private Long lastId;
	/**
	 * 来源类型:13:利息记录(利息包释放的记录)
	 */
	private List<Integer> sourceTypes;
   }
