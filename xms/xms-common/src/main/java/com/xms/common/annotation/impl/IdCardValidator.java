package com.xms.common.annotation.impl;


import com.xms.common.annotation.IdCard;
import com.xms.common.utils.IdCardUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public void initialize(IdCard idCard) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || "".equals(value)) {
            return false;
        }
        int s = 15;
        if (value.length() == s) {
            return IdCardUtil.validate15IdCard(value);
        }
        int s1 = 18;
        if (value.length() == s1) {
            return IdCardUtil.validate18IdCard(value);
        }
        return false;
    }

}
