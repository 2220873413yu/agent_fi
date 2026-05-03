package com.xms.common.annotation.impl;

import com.xms.common.annotation.ValidDiyStatus;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * string类型数组校验
 *
 * @author: renengadePISTA
 * @createDate: 2023/9/11
 */
public class ValidDiyStringStatusValidator implements ConstraintValidator<ValidDiyStatus, String> {
	private Set<String> set = new HashSet<>();
	/**
	 * Implements the validation logic.
	 * The state of {@code value} must not be altered.
	 * <p>
	 * This method can be accessed concurrently, thread-safety must be ensured
	 * by the implementation.
	 *
	 * @param value   object to validate
	 * @param context context in which the constraint is evaluated
	 * @return {@code false} if {@code value} does not pass the constraint
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return set.contains(value);
	}

	/**
	 * Initializes the validator in preparation for
	 * {@link #isValid(Object, ConstraintValidatorContext)} calls.
	 * The constraint annotation for a given constraint declaration
	 * is passed.
	 * <p>
	 * This method is guaranteed to be called before any use of this instance for
	 * validation.
	 * <p>
	 * The default implementation is a no-op.
	 *
	 * @param constraintAnnotation annotation instance for a given constraint declaration
	 */
	@Override
	public void initialize(ValidDiyStatus constraintAnnotation) {
		String[] values = constraintAnnotation.strValues();
		Collections.addAll(set, values);
	}
}
