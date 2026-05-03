package com.xms.app.handler;

import com.xms.common.result.ResponseWrap;
import com.xms.common.result.ResponseCode;


public class CustomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private ResponseWrap<String> response;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(ResponseWrap<String> response) {
        this.response = response;
    }

    public CustomException(ResponseCode responseCode) {
        this.response = ResponseWrap.dataError(responseCode);
    }
    
    public CustomException(ResponseCode responseCode,String replace) {
        this.response = ResponseWrap.dataError(responseCode, replace);
    }

    public CustomException(String msg, Throwable t) {
        super(msg, t);
    }

    public ResponseWrap<String> getResponse() {
        return response;
    }

    public CustomException setResponse(ResponseWrap<String> Response) {
        this.response = Response;
        return this;
    }
}
