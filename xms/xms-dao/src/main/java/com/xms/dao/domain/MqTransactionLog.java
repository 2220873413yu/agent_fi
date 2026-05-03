package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * mq可靠投递日志对象 t_mq_transaction_log
 *
 * @author xms
 * @date 2024-07-09
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_mq_transaction_log")
public class MqTransactionLog  {
    private static final long serialVersionUID = 1L;

    /** 主键，目前版本除非断电，否则不重置 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 事务id，唯一消息ID，发送完删除,事务消息需要 */
    @Excel(name = "事务id，唯一消息ID，发送完删除,事务消息需要")
    @ApiModelProperty(value = "事务id，唯一消息ID，发送完删除,事务消息需要")
    private String transactionId;
    /** 日志body内容 */
    @Excel(name = "日志body内容")
    @ApiModelProperty(value = "日志body内容")
    private  byte[] log;
	/**
	 * 时间戳,毫秒级
	 */
	private Long createTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("transactionId", getTransactionId())
            .append("log", getLog())
            .append("createTime", getCreateTime())
        .toString();
    }
}
