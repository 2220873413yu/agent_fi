package com.xms.common.exception;

import com.xms.common.config.LocaleContextHolder;
import com.xms.common.constant.Constants;
import com.xms.common.result.ResponseCode;

/**
 * 业务异常
 */
public final class ServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 错误码
	 */
	private Integer code = 500;

	/**
	 * 错误提示
	 */
	private String message;

	/**
	 * 错误明细，内部调试错误
	 * <p>
	 * 和 {@link CommonResult#getDetailMessage()} 一致的设计
	 */
	private String detailMessage;

	/**
	 * 空构造方法，避免反序列化问题
	 */
	public ServiceException() {
	}

	public ServiceException(String message) {
		this.message = message;
	}

	public ServiceException(String message, Integer code) {
		this.message = message;
		this.code = code;
	}

/*	public ServiceException(ResponseCode param) {
		this.message = param.getMsg();
		this.code = param.getCode();
	}*/

	public ServiceException(ResponseCode param) {
		//获取threadloca现在是什么语言然后返回不同的msg
		this.message = param.getMsg();
		this.code = param.getCode();
	}

	public String getDetailMessage() {
		return detailMessage;
	}

	public ServiceException setDetailMessage(String detailMessage) {
		this.detailMessage = detailMessage;
		return this;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public ServiceException setMessage(String message) {
		this.message = message;
		return this;
	}

	public Integer getCode() {
		return code;
	}
}
