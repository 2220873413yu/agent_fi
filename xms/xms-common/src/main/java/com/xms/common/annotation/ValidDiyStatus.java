package com.xms.common.annotation;

/**
 * 校验只允许输入几个固定的值
 *
 * @author MIER
 */

import com.xms.common.annotation.impl.ValidDiyStringStatusValidator;
import com.xms.common.annotation.impl.ValidDiyValueStatusValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Documented
//指定校验器，这里不指定时，就需要在初始化时指定
@Constraint(
	validatedBy = {ValidDiyStringStatusValidator.class, ValidDiyValueStatusValidator.class}
)
public @interface ValidDiyStatus {
	// 默认的提示内容
	String message() default "必须提交指定的值!";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	//数值数组，提交的值只能是数组里面
	int[] values() default {};

	//字符串数组，提交的值只能是数组里面
	String[] strValues() default {};

}
