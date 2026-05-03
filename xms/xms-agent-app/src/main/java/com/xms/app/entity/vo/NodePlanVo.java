package com.xms.app.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 认购节点配置VO
 *
 * @author xms
 * @date 2026-01-16
 */
@Data
public class NodePlanVo {

	/** 主键id */
	private Long id;
	/** 节点等级 0:无,1:白银节点,2:黄金节点,3:翡翠节点,4:钻石节点 */
	private Long nodeLevel;
	/** 节点名称_英文 */
	private String nodeNameEn;
	/** 节点名称_繁体中文 */
	private String nodeNameHk;
	/** 节点名称_日本語 */
	private String nodeNameJa;
	/** 节点名称_韩文 */
	private String nodeNameKr;
	/** 节点名称_泰文 */
	private String nodeNameTh;
	/** 节点名称_越南 */
	private String nodeNameVi;
	/** 认购金额（美元） */
	private BigDecimal purchaseAmount;
	/** 总库存（个） */
	private Long totalQuota;
	/** 锁定库存（支付中） */
	private Long lockedQuota;
	/** 确认成功的销量 */
	private Long soldQuota;
	/** 分红比例（百分比） */
	private BigDecimal shareRatio;
	/** KMC 分红上限 */
	private String kmcBonusLimit;
	/** 排序值 */
	private Long sortOrder;

	/**
	 * 平台手续费分红 例如20就是20%
	 */
	private String strFeeRate;
	/**
	 * 商城利润分红 例如10就是10%
	 */
	private String strProfitRate;
}
