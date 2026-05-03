package com.xms.dao.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import com.xms.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 矿机管理费配置对象 t_mining_mgmt_fee_config
 *
 * @author xms
 * @date 2026-02-27
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mining_mgmt_fee_config")
public class MiningMgmtFeeConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键id */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 管理费池比例(单位%)，默认20=20% */
    @Excel(name = "管理费池比例(单位%)，默认20=20%")
    @ApiModelProperty(value = "管理费池比例(单位%)，默认20=20%")
    private BigDecimal feePoolRatio;
    /** 县代理总比例(单位%) */
    @Excel(name = "县代理总比例(单位%)")
    @ApiModelProperty(value = "县代理总比例(单位%)")
    private BigDecimal agentDiffCountyRatio;
    /** 区代理总比例(单位%) */
    @Excel(name = "区代理总比例(单位%)")
    @ApiModelProperty(value = "区代理总比例(单位%)")
    private BigDecimal agentDiffAreaRatio;
    /** 市代理总比例(单位%) */
    @Excel(name = "市代理总比例(单位%)")
    @ApiModelProperty(value = "市代理总比例(单位%)")
    private BigDecimal agentDiffCityRatio;
    /** 省代理总比例(单位%) */
    @Excel(name = "省代理总比例(单位%)")
    @ApiModelProperty(value = "省代理总比例(单位%)")
    private BigDecimal agentDiffProvinceRatio;
    /** 全国代理总比例(单位%) */
    @Excel(name = "全国代理总比例(单位%)")
    @ApiModelProperty(value = "全国代理总比例(单位%)")
    private BigDecimal agentDiffNationalRatio;
    /** 全国代理平级奖比例(单位%)：取全国级差奖励的X% */
    @Excel(name = "全国代理平级奖比例(单位%)：取全国级差奖励的X%")
    @ApiModelProperty(value = "全国代理平级奖比例(单位%)：取全国级差奖励的X%")
    private BigDecimal nationalSameLevelRatio;
    /** 平台管理费比例(单位%) */
    @Excel(name = "平台管理费比例(单位%)")
    @ApiModelProperty(value = "平台管理费比例(单位%)")
    private BigDecimal platformFeeRatio;
    /** 直推奖励比例(单位%) */
    @Excel(name = "直推奖励比例(单位%)")
    @ApiModelProperty(value = "直推奖励比例(单位%)")
    private BigDecimal directPushRatio;
    /** 间推奖励比例(单位%) */
    @Excel(name = "间推奖励比例(单位%)")
    @ApiModelProperty(value = "间推奖励比例(单位%)")
    private BigDecimal indirectPushRatio;
    /** 服务中心比例(单位%) */
    @Excel(name = "服务中心比例(单位%)")
    @ApiModelProperty(value = "服务中心比例(单位%)")
    private BigDecimal serviceCenterRatio;

	@TableField(exist = false)
	private String createBy;
	@TableField(exist = false)
	private String updateBy;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("feePoolRatio", getFeePoolRatio())
            .append("agentDiffCountyRatio", getAgentDiffCountyRatio())
            .append("agentDiffAreaRatio", getAgentDiffAreaRatio())
            .append("agentDiffCityRatio", getAgentDiffCityRatio())
            .append("agentDiffProvinceRatio", getAgentDiffProvinceRatio())
            .append("agentDiffNationalRatio", getAgentDiffNationalRatio())
            .append("nationalSameLevelRatio", getNationalSameLevelRatio())
            .append("platformFeeRatio", getPlatformFeeRatio())
            .append("directPushRatio", getDirectPushRatio())
            .append("indirectPushRatio", getIndirectPushRatio())
            .append("serviceCenterRatio", getServiceCenterRatio())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("deleted", getDeleted())
        .toString();
    }
}
