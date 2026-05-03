package com.xms.web.exception;

import com.xms.common.constant.HttpStatus;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.DemoModeException;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.List;

/**
 * 全局异常处理器
 *
 * @author MIER
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = DuplicateKeyException.class)
	@ResponseBody
	public ResultPista<String> exceptionHandler(DuplicateKeyException e) {
		log.error("数据写入异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_109);
	}

	/**
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = DataIntegrityViolationException.class)
	public ResultPista<String> exceptionHandler(DataIntegrityViolationException e) {
		log.error("发生类型转换异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_115);
	}

	/**
	 * 权限校验异常
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResultPista handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
		return ResultPista.fail(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
	}

	/**
	 * 请求方式不支持
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResultPista handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest request) {
		String requestURI = request.getRequestURI();

		log.error("请求参数:" + request.getParameterMap());
		log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return ResultPista.fail(e.getMessage());
	}

	/**
	 * 业务异常
	 */
	@ExceptionHandler(ServiceException.class)
	public ResultPista handleServiceException(ServiceException e, HttpServletRequest request) {
		log.error(e.getMessage(), e);
		Integer code = e.getCode();
		return StringUtils.isNotNull(code) ? ResultPista.fail(code, e.getMessage()) : ResultPista.fail(e.getMessage());
	}

	/**
	 * 拦截未知的运行时异常
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResultPista handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生未知异常.", requestURI, e);
		return ResultPista.fail(e.getMessage());
	}

	/**
	 * 系统异常
	 */
	@ExceptionHandler(Exception.class)
	public ResultPista handleException(Exception e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生系统异常.", requestURI, e);
		return ResultPista.fail(e.getMessage());
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(BindException.class)
	public ResultPista handleBindException(BindException e) {
		log.error(e.getMessage(), e);
		String message = e.getAllErrors().get(0).getDefaultMessage();
		return ResultPista.fail(message);
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error(e.getMessage(), e);
		List<ObjectError> errors = e.getBindingResult()
			.getAllErrors();
		String msg = getValidExceptionMsg(errors);
		return ResultPista.fail(msg);
	}
	private String getValidExceptionMsg(List<ObjectError> errors) {
		if (CollectionUtils.isNotEmpty(errors)) {
			StringBuilder sb = new StringBuilder();
			errors.forEach(error -> {
				if (error instanceof FieldError) {
					sb.append(((FieldError) error).getField()).append(":");
				}
				sb.append(error.getDefaultMessage()).append(";");
			});
			String msg = sb.toString();
			msg = org.apache.commons.lang3.StringUtils.substring(msg, 0, msg.length() - 1);
			return msg;
		}
		return null;
	}
	/**
	 * 演示模式异常
	 */
	@ExceptionHandler(DemoModeException.class)
	public ResultPista handleDemoModeException(DemoModeException e) {
		return ResultPista.fail("演示模式，不允许操作");
	}


	/**
	 * SQL异常
	 */
	@ExceptionHandler(SQLException.class)
	public ResultPista<String> handleHttpRequestMethodNotSupported(SQLException e) {
		log.error(e.getMessage());
		return ResultPista.fail(ResponseCode.CODE_110);
	}

	/**
	 * 处理空指针的异常
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = NullPointerException.class)
	public ResultPista<String> exceptionHandler(NullPointerException e) {
		log.error("发生空指针异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_111);
	}

	/**
	 * 处理类型转换异常
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = NumberFormatException.class)
	public ResultPista<String> exceptionHandler(NumberFormatException e) {
		log.error("发生类型转换异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_112);
	}

}
