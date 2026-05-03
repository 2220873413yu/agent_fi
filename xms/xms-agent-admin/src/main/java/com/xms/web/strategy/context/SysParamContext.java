package com.xms.web.strategy.context;

import com.xms.dao.entity.domain.SysPara;
import lombok.Data;

/**
 * AsyncContext
 */
@Data
public class SysParamContext extends StrategyContext {

	private static final long serialVersionUID = 1L;

	private SysPara sysPara;


}
