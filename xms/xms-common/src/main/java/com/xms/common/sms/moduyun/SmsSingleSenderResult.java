package com.xms.common.sms.moduyun;

/**
 * @author MIER
 */
public class SmsSingleSenderResult {

	/**
	 * {
	 * 		"result": 0,
	 * 		"errmsg": "OK",
	 * 		"ext": "",
	 * 		"sid": "xxxxxxx",
	 * 		"fee": 1
	 *        }
	 */
	public int result;
	public String errMsg = "";
	public String ext = "";
	public String sid = "";

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	@Override
	public String toString() {
		return String.format(
			"SmsSingleSenderResult\nresult %d\nerrMsg %s\next %s\nsid %s\n",
			result, errMsg, ext, sid);
	}

}
