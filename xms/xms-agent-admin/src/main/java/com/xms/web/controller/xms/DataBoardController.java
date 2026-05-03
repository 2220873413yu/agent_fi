package com.xms.web.controller.xms;

import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.web.service.DataBoardsService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页数据统计 控制器
 * 2023-09-07
 */
@Api(tags = "首页数据看板")
@RestController
@RequestMapping("/xms/dataBoard")
public class DataBoardController extends BaseController {

	@Autowired
	private DataBoardsService dataBoardsServiceImpl;

	/**
	 * 查询用户相关数据
	 *
	 * @return
	 */
	@GetMapping("/getUserDataBoard")
	public AjaxResult getUserTotal() throws Exception {
		return success(dataBoardsServiceImpl.getUserDataBoard());
	}

	/**
	 * 查询资产统计
	 *
	 * @return
	 */
	@GetMapping("/listGroupTotal")
	public AjaxResult listGroupTotal() throws Exception {
		return success(dataBoardsServiceImpl.listGroupTotal());
	}
	/**
	 * 查询拨比统计
	 *
	 * @return
	 */
	@GetMapping("/listOrderGroupTotal")
	public AjaxResult listOrderGroupTotal() throws Exception {
		//return success(dataBoardsServiceImpl.listOrderGroupTotal());
		return success();
	}
}
