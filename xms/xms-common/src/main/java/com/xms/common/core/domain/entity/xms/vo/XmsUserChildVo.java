package com.xms.common.core.domain.entity.xms.vo;

import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class XmsUserChildVo {


    @Excel(name = "地址")
    private String address;
    /**
     * 团队等级
     */
    @Excel(name = "团队等级")
    private Integer teamGrade;
    /**
     * 团队业绩
     */
    @Excel(name = "团队业绩")
    private BigDecimal achievement;

    @Excel(name = "团队人数")
    private Integer teamNum = 0;

    //个人业绩
    private BigDecimal personMent;
}
