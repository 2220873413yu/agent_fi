package com.xms.common.annotation;

import com.xms.common.annotation.impl.IdCardValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = IdCardValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IdCard {

    String message() default "请填写正确的身份证号码";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
