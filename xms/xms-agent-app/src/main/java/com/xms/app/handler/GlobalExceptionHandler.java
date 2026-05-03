package com.xms.app.handler;

import cn.hutool.core.io.IORuntimeException;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: GlobalExceptionHandler
 * @Date: 2021/6/10 17:55
 * @Description: 异常处理
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	public ResultPista<String> handleCustomException(Throwable e) {
		if (e instanceof MissingServletRequestParameterException) {
			String msg = MessageFormat.format("缺少参数{0}",
				((MissingServletRequestParameterException) e)
					.getParameterName());
			log.error(msg);
			return ResultPista.fail(ResponseCode.CODE_1000.getCode(), msg);
		} else if (e instanceof ConstraintViolationException) {
			Set<ConstraintViolation<?>> sets = ((ConstraintViolationException) e)
				.getConstraintViolations();
			if (CollectionUtils.isNotEmpty(sets)) {
				StringBuilder sb = new StringBuilder();
				sets.forEach(error -> {
					if (error instanceof FieldError) {
						sb.append(((FieldError) error).getField()).append(":");
					}
					sb.append(error.getMessage()).append(";");
				});
				String msg = sb.toString();
				msg = StringUtils.substring(msg, 0, msg.length() - 1);
				log.error(msg);
				return ResultPista.fail(ResponseCode.CODE_1000.getCode(), msg);
			}
		} else if (e instanceof BindException) {
			List<ObjectError> errors = ((BindException) e).getBindingResult()
				.getAllErrors();
			String msg = getValidExceptionMsg(errors);
			log.error(msg);
			return ResultPista.fail(ResponseCode.CODE_1000.getCode(), msg);
		} else if (e instanceof CustomException) {
			CustomException c = ((CustomException) e);
			log.error(c.getResponse().getMsg());
			return ResultPista.fail(c.getResponse().getCode(), c.getResponse().getMsg());
		} else if (e instanceof ServiceException) {
			ServiceException c = ((ServiceException) e);
			log.error(c.getMessage());
			return ResultPista.fail(c.getCode(), c.getMessage());
		}
		log.error("服务器异常:", e);
		return ResultPista.fail(ResponseCode.CODE_500.getCode(), e.getMessage());
	}



	/**
	 * 请求参数 数据格式异常
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResultPista<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
																	 HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求参数:" + request.getParameterMap());
		log.error("请求地址'{}'", requestURI);
		log.error(e.getMessage());
		return ResultPista.fail(ResponseCode.CODE_108);
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
			msg = StringUtils.substring(msg, 0, msg.length() - 1);
			return msg;
		}
		return null;
	}

	/**
	 * 请求方式不支持
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResultPista<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                   HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求参数:" + request.getParameterMap());
		log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return ResultPista.fail(e.getMessage());
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
	@ResponseBody
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
	@ResponseBody
	public ResultPista<String> exceptionHandler(NumberFormatException e) {
		log.error("发生类型转换异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_112);
	}

	/**
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = UnknownHostException.class)
	@ResponseBody
	public ResultPista<String> exceptionHandler(UnknownHostException e) {
		log.error("未知主机异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_113);
	}

	/**
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = IORuntimeException.class)
	@ResponseBody
	public ResultPista<String> exceptionHandler(IORuntimeException e) {
		log.error("未知主机异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_113);
	}

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
	@ExceptionHandler(value = MysqlDataTruncation.class)
	@ResponseBody
	public ResultPista<String> exceptionHandler(MysqlDataTruncation e) {
		log.error("未知主机异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_115);
	}

	/**
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = DataIntegrityViolationException.class)
	@ResponseBody
	public ResultPista<String> exceptionHandler(DataIntegrityViolationException e) {
		log.error("发生类型转换异常！原因是:", e);
		return ResultPista.fail(ResponseCode.CODE_115);
	}
}
