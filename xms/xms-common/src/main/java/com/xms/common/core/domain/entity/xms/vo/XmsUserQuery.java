package com.xms.common.core.domain.entity.xms.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.xms.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XmsUserQuery {

    private Long id;

    @Excel(name = "地址")
    private String address;
    //上级的地址 不保存到db，临时使用
    @TableField(exist = false)
    private String refAddress;

    @Excel(name = "邀请码")
    private String invitationCode;

    //是节点 0表示不是，1表示是
    @Excel(name = "是节点")
    private Integer isNode = 0;
    //是黑名单 0表示不是，1表示是
    @Excel(name = "是黑名单")
    private Integer isBlack;

    @Excel(name = "帕值")
    private BigDecimal catVal;

    private BigDecimal smallAchieveMent;
    private List<XmsUserChildVo> childTeamList;
    /**
     * 买门票 0表示没买 1表示买了
     */
    @Excel(name = "买门票")
    private Integer buyTickets = 0;

    /**
     * 团队等级
     */
    @Excel(name = "团队等级")
    private Integer teamGrade;

    /**
     * 上级层级链
     */
    @Excel(name = "上级层级链")
    private String parentChain;

    /**
     * 上级id
     */
    @Excel(name = "上级id")
    private Long referorId;
    /**
     * 层级
     */
    @Excel(name = "层级")
    private Long algebra;
    /**
     * 直推人数
     */
    @Excel(name = "直推人数")
    private Integer dpNum = 0;
    /**
     * 支付密码
     */
    @Excel(name = "支付密码")
    private String payPassword;

    @Excel(name = "usdt余额")
    private BigDecimal usdtAmount;
    @Excel(name = "xms余额")
    private BigDecimal xmsAmount;
    @Excel(name = "fil余额")
    private BigDecimal filAmount;


    @Excel(name = "团队业绩")
    private BigDecimal achieveMent;
    @Excel(name = "个人业绩")
    private BigDecimal personMent;

    @Excel(name = "用户登录token")
    private String userToken;

    //可买等级，默认1级
    private Integer mealLev = 1;

    //版本号，用来控制金额的
    private Integer version = 0;

    //xms地址
    private String xmsAddress;

    //fil地址
    private String filAddress;
    //u盾地址
    private String udunAddress;

    /**
     * 增加伞下投资30天以上的总有效投资金额的统计
     */
    @TableField(exist = false)
    private BigDecimal paAmount;

    //冻结xms
    @TableField(exist = false)
    @Excel(name = "冻结xms")
    private BigDecimal freezeAmount;

    @Excel(name = "套餐的最大金额")
    private BigDecimal mealAmount;

    @Excel(name = "持仓天数")
    private Integer bond_days;

    @Excel(name = "最低等级")
    private Integer minGrade;
}
