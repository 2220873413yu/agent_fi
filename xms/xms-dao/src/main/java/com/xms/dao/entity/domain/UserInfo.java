package com.xms.dao.entity.domain;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseXmsEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息表。
 *
 * <p>初始化框架阶段只保留基础用户、关系、等级、业绩和提现控制字段。
 * 节点等级、全球分红、OpenAI 扣费等强业务字段不放在基础实体里。</p>
 *
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_info")
@ApiModel(value = "UserInfo对象", description = "用户信息表")
public class UserInfo extends BaseXmsEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID。
	 */
	@TableId(value = "user_id", type = IdType.AUTO)
	@Excel(name = "用户ID", sort = 1)
	private Long userId;

	/**
	 * 用户编码。
	 */
	private String userCode;

	/**
	 * 钱包地址。
	 */
	@Excel(name = "钱包地址", sort = 2, width = 40)
	private String account;

	/**
	 * 头像。
	 */
	private String avatar;

	/**
	 * 邮箱。
	 */
	private String email;

	/**
	 * 邀请用户编码。
	 */
	private String inviteUserCode;

	/**
	 * 邀请用户ID。
	 */
	@Excel(name = "邀请用户ID", sort = 5)
	private Long inviteUserId;

	/**
	 * 真实等级。
	 */
	private Integer gameLevel;

	/**
	 * 赠送等级。
	 */
	private Integer minGameLevel;

	/**
	 * 状态：1 正常，2 冻结。
	 */
	@Excel(name = "账户状态", sort = 8, dictType = "t_user_info_status")
	private Integer status;

	/**
	 * 是否有效用户：0 否，1 是。
	 */
	private Integer isValid;

	/**
	 * 直推用户数。
	 */
	@Excel(name = "直推用户数", sort = 6)
	private Integer subNum;

	/**
	 * 团队用户数。
	 */
	@Excel(name = "团队用户数", sort = 7)
	private Integer umbrellaNum;

	/**
	 * 个人托管业绩。
	 */
	private BigDecimal performance;

	/**
	 * 直推托管业绩。
	 */
	private BigDecimal subPerformance;

	/**
	 * 团队托管业绩兼容字段。
	 */
	private BigDecimal performanceMining;

	/**
	 * 伞下团队托管业绩。
	 */
	private BigDecimal umbrellaPerformance;

	/**
	 * 小区托管业绩。
	 */
	private BigDecimal communityPerformance;

	/**
	 * 父级链。
	 */
	private String parentChain;

	/**
	 * USDT 提现开关：1 关，2 开。
	 */
	@Excel(name = "USDT提现开关", dictType = "biz_open_or_close", sort = 9)
	private Integer withdrawalOpenOrClose;

	/**
	 * 最近登录 IP。
	 */
	private String lastLoginIp;

	/**
	 * 后台用户备注。
	 */
	@Excel(name = "备注", sort = 10, width = 40)
	@ApiModelProperty(value = "后台用户备注")
	private String remark;

	/**
	 * 删除标记：0 正常，1 删除。
	 */
	private Integer deleted;

	/**
	 * 后台树结构展示使用的父级ID，不落库。
	 */
	@TableField(exist = false)
	private Long parentId;

	/**
	 * 最大区业绩，查询或展示时使用，不落库。
	 */
	@TableField(exist = false)
	private BigDecimal maxLegPerformance;

	/**
	 * 获取父级用户ID列表。
	 *
	 * @return 父级用户ID列表；父级链为空时返回空列表
	 */
	public List<Long> getParentIds() {
		if (StrUtil.isBlank(this.getParentChain())) {
			return new ArrayList<>();
		}
		return Arrays.stream(this.getParentChain().split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(Long::valueOf)
			.collect(Collectors.toList());
	}
}
