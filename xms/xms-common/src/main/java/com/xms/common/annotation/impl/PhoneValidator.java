package com.xms.common.annotation.impl;


import com.xms.common.annotation.Phone;
import io.netty.util.internal.StringUtil;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @Date: 2019/5/28
 * @Time: 15:09
 * @Description:
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    private final Pattern pattern = Pattern.compile("^1[0-9]{10}$");

    @Override
    public void initialize(Phone phone) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtil.isNullOrEmpty(value)) return false;
        return pattern.matcher(value).matches();
    }
}
