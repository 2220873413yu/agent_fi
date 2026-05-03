package com.xms.app.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的质押信息
 * @author xms
 * @date 2023/6/12
 */
@Data
public class MyStakeInfoDto {

	/**
	 * 锁仓中OORT
	 */
	private BigDecimal bucketAmount = BigDecimal.ZERO;

	/**
	 * 今日产出
	 */
	private BigDecimal todayOutReward = BigDecimal.ZERO;

	/**
	 * 今日锁仓释放
	 */
	private BigDecimal todayOutBucketAmount = BigDecimal.ZERO;

	/**
	 * 未到期质押份数
	 */
	private Long haveOrder = 0L;
}
