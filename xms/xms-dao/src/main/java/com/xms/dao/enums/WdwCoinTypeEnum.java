package com.xms.dao.enums;


/**
 * @author MIER
 */

public enum WdwCoinTypeEnum {

	/**
	 *
	 */
	USDTBS0(1, "USDTBS0"),
	FSNBS0(3, "FSNBS0"),
	;


	String name;
	Integer key;

	WdwCoinTypeEnum(Integer key, String name) {
		this.name = name;
		this.key = key;
	}

	WdwCoinTypeEnum() {

	}

	public static WdwCoinTypeEnum getStatus(Integer key) {
		for (WdwCoinTypeEnum queueEnums : values()) {
			if (key.equals(queueEnums.key)) {
				return queueEnums;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

}
