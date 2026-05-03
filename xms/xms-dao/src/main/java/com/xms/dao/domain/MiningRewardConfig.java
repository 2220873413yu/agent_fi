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
 * 矿机奖励分配配置对象 t_mining_reward_config
 *
 * @author xms
 * @date 2026-02-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mining_reward_config")
public class MiningRewardConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 奖励角色 1:直推,2:间推,3:市代理,4:省代理,5:全国代理 */
    @Excel(name = "奖励角色 1:直推,2:间推,3:市代理,4:省代理,5:全国代理")
    @ApiModelProperty(value = "奖励角色 1:直推,2:间推,3:市代理,4:省代理,5:全国代理")
    private Integer rewardLevel;
    /** 奖励比例 例如:1代表1% */
    @Excel(name = "奖励比例 例如:1代表1%")
    @ApiModelProperty(value = "奖励比例 例如:1代表1%")
    private BigDecimal rewardRatio;

	@TableField(exist = false)
	private Integer deleted;
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("rewardLevel", getRewardLevel())
            .append("rewardRatio", getRewardRatio())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
        .toString();
    }
}
