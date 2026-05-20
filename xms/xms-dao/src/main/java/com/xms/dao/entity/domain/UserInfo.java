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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表
 * </p>
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
	 * 用户id
	 */
	@TableId(value = "user_id", type = IdType.AUTO)
	@Excel(name = "用户ID", sort = 1)
	private Long userId;


	/**
	 * 用户编码
	 */
	//@Excel(name = "用户编码", sort = 2)
	private String userCode;


	/**
	 * 节点等级
	 */
	@Excel(name = "节点等级", sort = 2, dictType = "t_node_plan_node_level")
	private Integer nodeLevel;

	/**
	 * 钱包地址
	 */
	@Excel(name = "钱包地址", sort = 2, width = 40)
	private String account;

	/**
	 * 团队业绩(节点数量)
	 */
	@Excel(name = "团队业绩(节点数量)", sort = 3)
	private BigDecimal nodeTeamPerformance;

	/**
	 * 直推业绩(节点数量)
	 */
	@Excel(name = "直推业绩(节点数量)", sort = 4)
	private BigDecimal subNodePerformance;

	/**
	 * 是否有效用户：0=无未完成托管订单，1=持有未完成托管订单
	 */
	//@Excel(name = "是否有效用户", sort = 4,dictType = "t_user_info_is_valid")
	private Integer isValid;

	/**
	 * 邀请用户编码
	 */
	//@Excel(name = "邀请用户编码", sort = 5)
	private String inviteUserCode;


	/**
	 * 邀请用户id
	 */
	@Excel(name = "邀请用户ID", sort = 5)
	private Long inviteUserId;

	/**
	 * 用户等级 (0:暂无,1:区代,2:县代,3:省代)
	 */
	//@Excel(name = "用户等级", sort = 8, dictType = "t_user_info_game_level")
	private Integer gameLevel;

	/**
	 * 赠送等级 (0:暂无,1:区代,2:县代,3:省代)
	 * 查询用户详情、查询直推用户列表时作为保底等级参与展示。
	 */
	//@Excel(name = "赠送等级", sort = 9, dictType = "t_user_info_game_level")
	private Integer minGameLevel;

	/**
	 * 管理员保底等级 (0:暂无,1:F1,2:F2,3:F3,4:F4,5:F5,6:F6,7:F7,8:F8,9:F9)
	 */
	//@Excel(name = "管理员保底等级", sort = 10, dictType = "t_user_info_game_level")
	private Integer adminGameLevel;

	/**
	 * 直推用户数
	 */
	@Excel(name = "直推用户数", sort = 6)
	private Integer subNum;

	/**
	 * 团队用户数
	 */
	@Excel(name = "团队用户数", sort = 7)
	private Integer umbrellaNum;

	/**
	 * 我的业绩(质押量)
	 */
	//@Excel(name = "我的业绩(质押量)", sort = 12)
	private BigDecimal performance;
	/**
	 * 小区业绩
	 */
	//@Excel(name = "小区业绩", sort = 14)
	private BigDecimal communityPerformance;
	/**
	 * 团队业绩(质押量)
	 */
	//@Excel(name = "团队业绩(质押量)", sort = 15)
	private BigDecimal umbrellaPerformance;

	/**
	 * 团队托管业绩兼容字段
	 */
	private BigDecimal performanceMining;

	/**
	 * 我的全球分红权重.
	 */
	private BigDecimal globalDividendWeight;

	/**
	 * 团队全球分红权重.
	 */
	private BigDecimal globalDividendUmbrellaWeight;

	/**
	 * 小区全球分红权重.
	 */
	private BigDecimal globalDividendCommunityWeight;

	/**
	 * 托管指定静态收益率，单位%，0表示按G7规则
	 */
	private BigDecimal stakeHostingStaticRate;

	/**
	 * OpenAI聊天扣费状态 0:未扣费 1:已扣费
	 */
	private Integer openAiPaidStatus;

	/**
	 * 团队节点业绩(销售额)
	 */
	@Excel(name = "团队节点业绩(销售额)", sort = 7)
	private BigDecimal umbrellaNodePerformance;


	/**
	 * 直推节点业绩(销售额)
	 */
	@Excel(name = "直推节点业绩(销售额)", sort = 7)
	private BigDecimal subUmbrellaNodePerformance;


	/**
	 * 后台拨付节点团队业绩(销售额)
	 */
	@Excel(name = "后台拨付节点团队业绩(销售额)", sort = 7)
	private BigDecimal adminUmbrellaNodePerformance;


	/**
	 *状态 1 正常 2 冻结
	 */
	@Excel(name = "账户状态", sort = 8,dictType = "t_user_info_status")
	private Integer status;



	/** 提现开关(1.关 2.开) */
	@Excel(name = "USDT提现开关",dictType = "biz_open_or_close",sort = 9)
	private Integer withdrawalOpenOrClose;

	/**
	 * 头像 废弃
	 */
	//@Excel(name = "头像", sort = 2)
	private String avatar;

	/**
	 * 邮箱 废弃
	 */
	//@Excel(name = "邮箱", sort = 3)
	private String email;




	/**
	 * 直推业绩(托管量)
	 */
	//@Excel(name = "直推业绩(废弃)", sort = 13)
	private BigDecimal subPerformance;



	@TableField(exist = false)
	private BigDecimal maxLegPerformance;

	/**
	 * 直推有效用户数(暂时废弃)
	 */
	//@Excel(name = "直推有效用户数", sort = 8)
	private Integer validSubNum;


	/**
	 * 团队用户数(有效)(暂时废弃)
	 */
	//@Excel(name = "团队用户数(有效)", sort = 8)
	private Integer validUmbrellaNum;



	/** 父级链 */
	//@Excel(name = "父级链")
	private String parentChain;


	/**
	 * 后台管理页面-树结构使用场景
	 */
	@TableField(exist = false)
	private Long parentId;
	/**
	 * 后台管理页面查询等级
	 */
	@TableField(exist = false)
	private Integer finaGameLevel;

	/**
	 * 最后登录ip
	 */
	private String lastLoginIp;

	/**
	 * 后台用户备注
	 */
	@Excel(name = "备注", sort = 10, width = 40)
	@ApiModelProperty(value = "后台用户备注")
	private String remark;



	/**
	 * 删除标志 0正常 1删除
	 */
	private Integer deleted;

	/**
	 * 获取父级用户ID列表
	 *
	 * 根据parentChain字段解析出所有父级用户的ID列表
	 * parentChain是以逗号分隔的父级用户ID字符串
	 *
	 * @return 父级用户ID列表，如果parentChain为空则返回空列表
	 */
	public List<Long> getParentIds() {
		if (StrUtil.isBlank(this.getParentChain())) {
			return new ArrayList<>();
		}
		// 解析成list<Long> 按照,号分割
		return Arrays.stream(this.getParentChain().split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(Long::valueOf)
			.collect(Collectors.toList());
	}
}
