package com.xms.web.service;

import java.util.Map;

/**
 * @author: renengadePISTA
 * @createDate: 2024/9/4
 */
public interface DataBoardsService {
	/**
	 * 统计会员相关数据看板
	 *
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getUserDataBoard() throws Exception;

	Map<String, Object> listGroupTotal() throws Exception;

	Map<String, Object> listOrderGroupTotal() throws Exception;
}
