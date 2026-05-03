package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserMoneySumDTO {
    private BigDecimal totalValidNum1;
    private BigDecimal totalValidNum2;
    private BigDecimal totalValidNum3;
}
