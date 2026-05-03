package com.xms.dao.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  binlog前后信息包装下
 *
 * @author yaojie
 * @since 2023-07-25
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMoneyCanalWrapper {

	private UserMoneyCanal before;
	private UserMoneyCanal after;

}
