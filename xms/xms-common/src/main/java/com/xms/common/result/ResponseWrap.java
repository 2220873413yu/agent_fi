package com.xms.common.result;

import com.xms.common.config.LocaleContextHolder;
import com.xms.common.constant.Constants;
import lombok.Data;

import java.util.Date;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

@Data
public class ResponseWrap<T> {
    /**
     * 请求成功
     */
    public static final int SUCCESS=200;
    /**
     * 请求数据不正确
     */
    public static final int DATAERROR=300;
    /**
     * 无权限访问
     */
    public static final int NOACCESS=400;
    /**
     * 服务器出错
     */
    public static final int SERVERERROR=500;
	/**
	 * 状态码
	 */
    private Integer code;
	/**
	 * 数据
	 */
    private T data;
	/**
	 * 消息
	 */
    private String msg;
	/**
	 * 错误码
	 */
	private Integer errorCode;
	/**
	 * 响应时间
	 */
	private Date currentTime;

	public static <T> ResponseWrap<T> successWithEmpty(){
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.SUCCESS);
		response.setData(null);
		response.setMsg(ResponseCode.CODE_200.getMsg());
		response.setErrorCode(ResponseCode.CODE_200.getCode());
		return response;
	}

	public static <T> ResponseWrap<T> success(T data){
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.SUCCESS);
		response.setData(data);
		response.setMsg(ResponseCode.CODE_200.getMsg());
		response.setErrorCode(ResponseCode.CODE_200.getCode());
		response.setCurrentTime(new Date());
		return response;
	}

	public static <T> ResponseWrap<T> dataError(ResponseCode responseCode, String... args){
		String desc	= responseCode.getMsg();
		Matcher m = compile("\\{(\\d)\\}").matcher(desc);
		while (m.find()) {
			desc = desc.replace(m.group(), args == null ? "" : args[Integer.parseInt(m.group(1))]);
		}
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.DATAERROR);
		response.setMsg(desc);
		response.setErrorCode(responseCode.getCode());
		return response;
	}

	public static <T> ResponseWrap<T> dataError(ResponseCode responseCode){
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.DATAERROR);
		response.setMsg(responseCode.getMsg());
		response.setErrorCode(responseCode.getCode());
		return response;
	}

	public static <T> ResponseWrap<T> serverError(ResponseCode responseCode){
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.SERVERERROR);
		response.setMsg(responseCode.getMsg());
		response.setErrorCode(responseCode.getCode());
		return response;
	}

	public static <T> ResponseWrap<T> serverError(ResponseCode responseCode, String... args){
		String desc;
		desc = responseCode.getMsg();
		Matcher m = compile("\\{(\\d)\\}").matcher(desc);
		while (m.find()) {
			desc = desc.replace(m.group(), args == null ? "" : args[Integer.parseInt(m.group(1))]);
		}
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.SERVERERROR);
		response.setMsg(desc);
		response.setErrorCode(responseCode.getCode());
		return response;
	}

	/**
	 * 登陆后才能访问
	 * @return
	 */
	public static <T> ResponseWrap<T> loginAuth(ResponseCode responseCode){
		ResponseWrap<T> response=new ResponseWrap<>();
		response.setCode(ResponseWrap.NOACCESS);
		response.setMsg(responseCode.getMsg());
		response.setErrorCode(responseCode.getCode());
		return response;
	}
}
