package com.xms.common.core.domain.entity.mcg;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseMcgEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * mcg用户对象 mcg_user
 *
 *
 * @date 2023-03-08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McgUser extends BaseMcgEntity {
    private static final long serialVersionUID = 1L;

    /**  */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户名称 */
    @Excel(name = "用户名称")
    private String name;
    /** 用户手机号 */
    @Excel(name = "用户手机号")
    private String phone;
    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;
    /** 邀请码 */
    @Excel(name = "邀请码")
    private String invitationCode;
    /** 用户等级 */
    @Excel(name = "用户等级")
    private Integer grade;
    /** 上级层级链 */
    @Excel(name = "上级层级链")
    private String parentChain;
    /** 上级id */
    @Excel(name = "上级id")
    private Long referorId;
    /** 层级 */
    @Excel(name = "层级")
    private Long algebra;
    /** 直推人数 */
    @Excel(name = "直推人数")
    private Integer dpNum;
    /** 账户余额 */
    @Excel(name = "账户余额")
    private BigDecimal balance;
    /** 收款地址 */
    @Excel(name = "收款地址")
    private String collectionAddress;
    /** 支付密码 */
    @Excel(name = "支付密码")
    private String payPassword;
    /** ETH地址 */
    @Excel(name = "ETH地址")
    private String ethAddress;
    /** TRX地址 */
    @Excel(name = "TRX地址")
    private String trxAddress;
    /** usdt余额 */
    @Excel(name = "usdt余额")
    private BigDecimal usdt;
    /** mcg余额 */
    @Excel(name = "mcg余额")
    private BigDecimal mcg;
    /** usdtTrx余额 */
    @Excel(name = "usdtTrx余额")
    private BigDecimal usdtTrx;
    /** 助记词 */
    @Excel(name = "助记词")
    private String mnemonic;
    /** 团队业绩 */
    @Excel(name = "团队业绩")
    private BigDecimal achievement;
    /** 个人业绩 */
    @Excel(name = "个人业绩")
    private BigDecimal personMent;
    /** 个人业绩 */
    @Excel(name = "用户登录token")
    private String userToken;
    /** 操作单号 */
    @Excel(name = "操作单号")
    private String opOrderNo;
    @Excel(name = "操作类型 1充值  2提现  3 直推分享奖  4 2-5代分享奖  5 2-10代分享奖  6级差奖  7 提现退回 8 闪兑  9提现手续费 10 修改余额 11 购买套餐  12 本金到期返还  13每日结息")
    private String opType;

}
