package com.xms.app.controller;


import com.github.pagehelper.PageInfo;
import com.xms.app.service.BannerService;
import com.xms.app.service.SysNoticeService;
import com.xms.common.annotation.Anonymous;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.entity.domain.Banner;
import com.xms.dao.entity.domain.Notice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告、轮播图 前端控制器
 */
@Api(tags = "系统公告")
@RestController
@RequestMapping("sysnotice")
public class SysNoticeController {

	@Autowired
	private SysNoticeService sysNoticeService;

	@Autowired
	private BannerService bannerService;

	/**
	 * 查询广告轮播图
	 * @param locale 语言 1:简体中文,2:英文,3:日文,4:韩文,5:越南语
	 * @return
	 */
	@ApiOperation(value = "查询广告轮播图")
	@GetMapping(value = "/getBannerList")
	@Anonymous
	public ResultPista<Banner> getBannerList(Integer locale) {
		if(locale !=null){
			Banner banner = bannerService.lambdaQuery()
				.eq(Banner::getType, locale)
				.last("limit 1")
				.one();
			return ResultPista.data(banner);
		}
		return  ResultPista.success();
	}

	/**
	 * 公告列表
	 *
	 * @param pageIndex 当前页 默认1
	 * @param type      语言类型 1:简体中文,2:英文,3:日文,4:韩文,5:越南语
	 * @param pageSize  每页长度 默认10(最大10)
	 * @return
	 */
	@ApiOperation(value = "公告列表")
	@GetMapping(value = "/getSysNoticeList")
	@Anonymous
	public ResultPista<PageInfo<Notice>> getSysNoticeList(
		@ApiParam(value = "当前页", required = true) @NotNull @RequestParam(defaultValue = "1") Integer pageIndex,
		@ApiParam(value = "每页长度", required = true) @NotNull @RequestParam(defaultValue = "10") Integer pageSize,
		Integer type) {
		type = type == null ? 1 : type;
		return ResultPista.data(sysNoticeService.getSysNoticeList(pageIndex, pageSize, type));
	}

	/**
	 * 根据公告id查询详情
	 *
	 * @param noticeId 公告id
	 * @return
	 */
	@ApiOperation(value = "公告详情")
	@GetMapping(value = "/getSysNoticeInfo")
	@Anonymous
	public ResultPista<Notice> getSysNoticeInfo(
		@ApiParam(value = "公告id", required = true) @NotNull @RequestParam() Integer noticeId) {
		return ResultPista.data(sysNoticeService.getSysNoticeInfo(noticeId));
	}


	/**
	 * 获取最新三条公告信息
	 *
	 * @param type 语言类型 1:简体中文,2:英文,3:日文,4:韩文,5:越南语
	 * @return
	 */
	@ApiOperation(value = "获取最新三条公告信息")
	@GetMapping(value = "/getLastNoticeInfo")
	@Anonymous
	public ResultPista<List<String>> getLastNoticeInfo(Integer type) {
		//Integer type = resolveNoticeType(acceptLanguage);
		type = type == null ? 1 : type;
		List<String> titles = sysNoticeService.lambdaQuery()
			.eq(Notice::getType, type)
			.eq(Notice::getStatus, SysConstant.ZERO)
			.orderByDesc(Notice::getNoticeId)
			.last("limit 3")
			.select(Notice::getNoticeTitle)
			.list().stream().map(Notice::getNoticeTitle)
			.collect(Collectors.toList());
		return ResultPista.data(titles);
	}




//	/**
//	 * 获取AI分析行情列表 分页5条
//	 * @param lastId lastId
//	 * @return
//	 */
//	@ApiOperation(value = "获取AI分析行情列表")
//	@GetMapping(value = "/getAiMarketList")
//	public ResultPista<List<AiMarketInsight>> getAiMarketList(Long lastId,Integer locale) {
//		if(locale == null || locale<=0){
//			return null;
//		}
//		List<AiMarketInsight> list = aiMarketInsightService.lambdaQuery()
//			.eq(AiMarketInsight::getType, locale)
//			.lt(Func.isNotEmpty(lastId), AiMarketInsight::getId, lastId)
//			.orderByDesc(AiMarketInsight::getId)
//			.orderByDesc(AiMarketInsight::getId).last(SysConstant.PAGE_LIMIT_10)
//			.select(AiMarketInsight::getId, AiMarketInsight::getTitle, AiMarketInsight::getSubTitle,
//				AiMarketInsight::getStatus, AiMarketInsight::getType, BaseEntity::getCreateTime,
//				AiMarketInsight::getImage, AiMarketInsight::getContentImage)
//			.list();
//		return ResultPista.data(list);
//	}

//	/**
//	 * 获取AI分析行情详情
//	 * @param id  id
//	 * @return
//	 */
//	@ApiOperation(value = "获取AI分析行情列表")
//	@GetMapping(value = "/getAiMarket")
//	public ResultPista<AiMarketInsight> getAiMarket(Long id) {
//		AiMarketInsight entity = aiMarketInsightService.lambdaQuery()
//			.eq(AiMarketInsight::getId, id)
//			.one();
//		return ResultPista.data(entity);
//	}
}

