package com.xms.app.server;

import org.yeauty.pojo.Session;

/**
 * @author: renengadePISTA
 * @createDate: 2023/10/19
 */
public interface WsStockService {
	void authTokenUser(Session session, String token, String loginType);

	void handerSub(Session session, String message);


	void sendGroupBySymbol(String symbol, String msg);

	void sendByUserId(Long userId, String msg);


	void sendByUserOpen(Session session);
}
