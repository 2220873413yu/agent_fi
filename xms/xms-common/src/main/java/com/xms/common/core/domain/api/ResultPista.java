
package com.xms.common.core.domain.api;

import com.xms.common.config.LocaleContextHolder;
import com.xms.common.constant.Constants;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.ObjectUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Optional;

/**
 * 统一API响应结果封装
 *
 *
 * @author MIER
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResultPista<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 默认为空消息
	 */
	private static final String DEFAULT_NULL_MESSAGE = "暂无承载数据";
	/**
	 * 默认成功消息
	 */
	private static final String DEFAULT_SUCCESS_MESSAGE = "操作成功";
	/**
	 * 默认失败消息
	 */
	private static final String DEFAULT_FAILURE_MESSAGE = "操作失败";
	/**
	 * 状态码
	 */
	private int code;
	/**
	 * 是否成功
	 */
	private boolean success;
	/**
	 * 承载数据
	 */
	private T data;
	/**
	 * 返回消息
	 */
	private String msg;

	private ResultPista(IResultCode resultCode) {
		this(resultCode, null, resultCode.getMessage());
	}

	private ResultPista(IResultCode resultCode, String resMsg) {
		this(resultCode, null, resMsg);
	}

	private ResultPista(IResultCode resultCode, T response) {
		this(resultCode, response, resultCode.getMessage());
	}

	private ResultPista(IResultCode resultCode, T response, String resMsg) {
		this(resultCode.getCode(), response, resMsg);
	}

	private ResultPista(int code, T response, String resMsg) {
		this.code = code;
		this.data = response;
		this.msg = resMsg;
		this.success = ResultCode.SUCCESS.code == code;
	}

	/**
	 * 判断返回是否为成功
	 *
	 * @param result Result
	 * @return 是否成功
	 */
	public static boolean isSuccess(@Nullable ResultPista<?> result) {
		return Optional.ofNullable(result)
			.map(x -> ObjectUtil.nullSafeEquals(ResultCode.SUCCESS.code, x.code))
			.orElse(Boolean.FALSE);
	}

	/**
	 * 判断返回是否为成功
	 *
	 * @param result Result
	 * @return 是否成功
	 */
	public static boolean isNotSuccess(@Nullable ResultPista<?> result) {
		return !ResultPista.isSuccess(result);
	}

	/**
	 * 返回R
	 *
	 * @param data 数据
	 * @param <T>  T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> data(T data) {
		return data(data, DEFAULT_SUCCESS_MESSAGE);
	}

	/**
	 * 返回R
	 *
	 * @param data 数据
	 * @param msg  消息
	 * @param <T>  T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> data(T data, String msg) {
		return data(HttpServletResponse.SC_OK, data, msg);
	}

	/**
	 * 返回R
	 *
	 * @param code 状态码
	 * @param data 数据
	 * @param msg  消息
	 * @param <T>  T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> data(int code, T data, String msg) {
		return new ResultPista<>(code, data, data == null ? DEFAULT_NULL_MESSAGE : msg);
	}

	/**
	 * 返回R
	 *
	 * @param <T> T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> success() {
		return new ResultPista<>(ResultCode.SUCCESS, DEFAULT_SUCCESS_MESSAGE);
	}

	/**
	 * 返回R
	 *
	 * @param msg 消息
	 * @param <T> T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> success(String msg) {
		return new ResultPista<>(ResultCode.SUCCESS, msg);
	}

	/**
	 * 返回R
	 *
	 * @param resultCode 业务代码
	 * @param <T>        T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> success(IResultCode resultCode) {
		return new ResultPista<>(resultCode);
	}

	/**
	 * 返回R
	 *
	 * @param resultCode 业务代码
	 * @param msg        消息
	 * @param <T>        T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> success(IResultCode resultCode, String msg) {
		return new ResultPista<>(resultCode, msg);
	}

	/**
	 * 返回R
	 *
	 * @param <T> T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail() {
		return new ResultPista<>(ResultCode.FAILURE, DEFAULT_FAILURE_MESSAGE);
	}


	/**
	 * 返回R
	 *
	 * @param responseCode 异常代码
	 * @param <T>          T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail(ResponseCode responseCode) {
		String message = responseCode.getMsg();
		return fail(responseCode.getCode(), message);
	}


	/**
	 * 返回R
	 *
	 * @param msg 消息
	 * @param <T> T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail(String msg) {
		return new ResultPista<>(ResultCode.FAILURE, msg);
	}


	/**
	 * 返回R
	 *
	 * @param code 状态码
	 * @param msg  消息
	 * @param <T>  T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail(int code, String msg) {
		return new ResultPista<>(code, null, msg);
	}


	/**
	 * 返回R
	 *
	 * @param resultCode 业务代码
	 * @param <T>        T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail(IResultCode resultCode) {
		return new ResultPista<>(resultCode);
	}

	/**
	 * 返回R
	 *
	 * @param resultCode 业务代码
	 * @param msg        消息
	 * @param <T>        T 泛型标记
	 * @return R
	 */
	public static <T> ResultPista<T> fail(IResultCode resultCode, String msg) {
		return new ResultPista<>(resultCode, msg);
	}

	/**
	 * 返回R
	 *
	 * @param flag 成功状态
	 * @return R
	 */
	public static <T> ResultPista<T> status(boolean flag) {
		return flag ? success(DEFAULT_SUCCESS_MESSAGE) : fail(DEFAULT_FAILURE_MESSAGE);
	}

}
