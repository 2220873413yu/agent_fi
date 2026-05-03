package com.xms.app.controller;

import com.xms.app.entity.resp.OssDoResp;
import com.xms.app.service.*;
import com.xms.common.annotation.Anonymous;
import com.xms.common.annotation.RateLimiter;
import com.xms.common.config.ProjectConfig;
import com.xms.common.config.ServerConfig;
import com.xms.common.config.oss.QiniuUtils;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.enums.LimitType;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.file.FileUploadUtils;
import com.xms.common.utils.file.FileUtils;
import com.xms.dao.domain.Contract;
import com.xms.dao.entity.bo.QiNiuBo;
import com.xms.dao.entity.domain.SysPara;
import com.xms.dao.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公用控制层
 */
@Api(tags = "公用控制层")
@RestController
@RequestMapping("/api/common")
public class CommonController {
	@Autowired
	private QiNiuService qiNiuService;
	@Autowired
	private QiniuUtils qiniuUtils;
	@Autowired
	private ISysParaService sysParaService;


	@Autowired
	private ServerConfig serverConfig;


	@Autowired
	private BizCommonService bizCommonService;

	@Autowired
	private IContractService contractService;
//
//	/**
//	 * 查询dfc的价格
//	 * @return
//	 */
//	@ApiOperation(value = "查询dfc的价格")
//	@GetMapping(value = "/getDfcPrice")
//	public ResultPista<BigDecimal> getDfcPrice() {
//		return ResultPista.data(bizCommonService.getDFcPrice());
//	}
//
//	/**
//	 * 查询dfc的价格
//	 * @return
//	 */
//	@ApiOperation(value = "查询dfc的价格")
//	@GetMapping(value = "/getOortPrice")
//	public ResultPista<BigDecimal> getOortPrice() {
//		return ResultPista.data(bizCommonService.getOortPrice());
//	}

//	/**
//	 * 获取等级配置
//	 * @return
//	 */
//	@ApiOperation(value = "获取等级配置")
//	@GetMapping(value = "/getLevelConfig")
//	public ResultPista<List<W3UserLevelConfig>> getLevelConfig() {
//		return ResultPista.data(w3UserLevelLogService.lambdaQuery()
//			.orderByAsc(W3UserLevelConfig::getLevel)
//			.list());
//	}
//
//	/**
//	 * 获取推荐奖励
//	 * @return
//	 */
//	@ApiOperation(value = "获取推荐奖励")
//	@GetMapping(value = "/getReferralRewar")
//	public ResultPista<ReferralRewardDto> getLevelConfig1() {
//		ReferralRewardDto entity = new ReferralRewardDto();
//		List<InteractRewardConfig> rewardConfigList = interactRewardConfigService.lambdaQuery()
//			.list();
//		BigDecimal up1= BigDecimal.ZERO;
//		BigDecimal l1= BigDecimal.ZERO;
//		BigDecimal l2= BigDecimal.ZERO;
//		for (InteractRewardConfig interactRewardConfig : rewardConfigList) {
//			if(interactRewardConfig.getLevel()==1){
//				up1=interactRewardConfig.getRewardRatio();
//			}else if(interactRewardConfig.getLevel()==2){
//				l1=interactRewardConfig.getRewardRatio();
//			}else if(interactRewardConfig.getLevel()==3){
//				l2=interactRewardConfig.getRewardRatio();
//			}
//		}
//		entity.setUp1(up1);
//		entity.setL1(l1);
//		entity.setL2(l2);
//		entity.setDirectReferral(new BigDecimal(sysParaService.getValue(ConstantSys.biz_destroy_order_referral_reward_ratio)));
//		return ResultPista.data(entity);
//	}

	/**
	 * 查询七牛token
	 * @return
	 */
	@ApiOperation(value = "查询七牛token")
	@GetMapping(value = "/qiniu/token")
	public ResultPista<QiNiuBo> getQiNiuToken() {
		return qiNiuService.getQiNiuToken();
	}

	/**
	 * 通用上传请求（单个）
	 */
	@PostMapping("/upload")
	@Anonymous
	public ResultPista uploadFile(@RequestParam MultipartFile file) throws Exception {
		try {
			// 上传文件路径
			String filePath = ProjectConfig.getUploadPath();
			// 上传并返回新文件名称
			String fileName = FileUploadUtils.upload(filePath, file);
			String qiniuUrl = qiniuUtils.upload(file, fileName);
			String url = serverConfig.getUrl() + fileName;
			return ResultPista.data(OssDoResp.builder()
				.url(qiniuUrl == null ? url : qiniuUrl)
				.fileName(fileName)
				.newFileName(FileUtils.getName(fileName))
				.originalFilename(file.getOriginalFilename())
				.build());
		} catch (Exception e) {
			e.printStackTrace();
			return ResultPista.fail(e.getMessage());
		}
	}

//	/**
//	 * 查询最新的版本号
//	 * @param deviceType 设备类型：ios android
//	 * @return
//	 */
//	@ApiOperation(value = "查询最新的版本号")
//	@GetMapping(value = "/getLastVersion")
//	@Anonymous
//	public ResultPista<SysVersionInfoBo> getLastVersion(
//		@ApiParam(value = "设备类型：ios android", required = true) @NotBlank @RequestParam(required = true) String deviceType) {
//		return ResultPista.data(sysVersionInfoService.getLastVersion(deviceType));
//	}

	/**
	 * 查询系统参数
	 * @param code WITHDRAWAL_MIN	提现最小数量
	 * mine_trade_time_limit	系统结算时间，不允许交易
	 * all_share_num	全网能量池分红数量
	 * DIRECT_RATIO	直推奖比例
	 * WITHDRAWAL_RATIO	提现手续费比例
	 * WITHDRAWAL_OPEN_OR_CLOSE	提现开关(1.关 2.开)
	 *             wechat_account	客服微信账号
	 * qq_account	客服QQ账号
	 * @return 返回code码对应的系统参数值
	 */
	@ApiOperation(value = "查询系统参数")
	@GetMapping(value = "/value")
	public ResultPista<String> getValue(
		@ApiParam(value = "编码", required = true) @NotBlank @RequestParam(required = true) String code) {
		return ResultPista.data(sysParaService.getValue(code));
	}


//
//	/**
//	 * 查询最新的3条公告
//	 * @param locale 语言 1:简体中文,2:英文,3:日文,4:韩文,5:越南语
//	 * @return
//	 */
//	@ApiOperation(value = "查询最新的公告")
//	@GetMapping(value = "/getLastNotice")
//	public ResultPista<List<Notice>> getLastNotice(Integer locale) {
//		if(locale !=null ){
//			List<Notice>  List = sysNoticeService.lambdaQuery()
//				.eq(Notice::getNoticeType, SysConstant.ONE)
//				.eq(Notice::getType, locale)
//				.select(Notice::getNoticeId,Notice::getNoticeTitle,Notice::getCreateTime)
//				.orderByDesc(Notice::getNoticeId)
//				.last("limit 3")
//				.list();
//			return ResultPista.data(List);
//		}
//		return  ResultPista.success();
//	}


//	/**
//	 * 查询公告列表 默认分5条
//	 * @param locale 语言 1:简体中文,2:英文,3:日文,4:韩文,5:越南语
//	 * @param type 公告类型 1:公告,2:咨询中心,3:基金百科
//	 * @param lastId
//	 * @return
//	 */
//	@ApiOperation(value = "查询最新的公告")
//	@GetMapping(value = "/getLastNotice")
//	public ResultPista<List<Notice>> getLastNotice(Integer locale,Integer type,Long lastId) {
//		if(locale !=null && type!=null){
//			List<Notice>  List = sysNoticeService.lambdaQuery()
//				.eq(Notice::getNoticeType, type)
//				.eq(Notice::getType, locale)
//				.select(Notice::getNoticeId,Notice::getNoticeTitle,Notice::getCreateTime,Notice::getRemark,Notice::getImage,
//					Notice::getContentImage)
//				.lt(Func.isNotEmpty(lastId), Notice::getNoticeId, lastId)
//				.orderByDesc(Notice::getNoticeId)
//				.last(SysConstant.PAGE_LIMIT)
//				.list();
//			return ResultPista.data(List);
//		}
//		return  ResultPista.success();
//	}

	/**
	 * 批量查询系统参数，多个以英文逗号拼接
	 *
	 * @param code WITHDRAWAL_MIN	提现最小数量
	 * mine_trade_time_limit	系统结算时间，不允许交易
	 * all_share_num	全网能量池分红数量
	 * DIRECT_RATIO	直推奖比例
	 * WITHDRAWAL_RATIO	提现手续费比例
	 * WITHDRAWAL_OPEN_OR_CLOSE	提现开关(1.关 2.开)
	 *wechat_account	客服微信账号
	 * qq_account	客服QQ账号
	 * @return
	 */
	@ApiOperation(value = "批量查询系统参数")
	@GetMapping(value = "/listParamValues")
	public ResultPista<List<SysPara>> listParamValues(
		@ApiParam(value = "编码", required = true) @NotBlank @RequestParam String code) {
		return ResultPista.data(sysParaService.listParamValues(code));
	}

	/**
	 * 获取系统所有的状态码，以及对应的msg
	 *
	 * @return
	 */
	@GetMapping(value = "/listCodeMsg")
	@RateLimiter(limitType = LimitType.DEFAULT, time = 2, count = 5)
	public ResultPista<List<Map<Integer, String>>> listCodeMsg() {
		List<Map<Integer, String>> list = Arrays.stream(ResponseCode.values())
			.map(enumValue -> {
					Map<Integer, String> param = new HashMap<>(2);
					param.put(enumValue.getCode(), enumValue.getMsg());
					return param;
				}
			).collect(Collectors.toList());
		return ResultPista.data(list);
	}

	/**
	 * 查询合同协议
	 *
	 * @param type 协议类型  4:手表认购协议
	 * @param locale 协议类型  1:简体中文,2:英文
	 * @return 返回协议信息
	 */
	@ApiOperation(value = "查询合同协议")
	@GetMapping(value = "/getContract")
	@Anonymous
	public ResultPista<Contract> getContract(Integer type,Integer locale) {
		if(type == null || locale == null){
			return ResultPista.fail("param error");
		}
		Contract contract = contractService.lambdaQuery()
			.eq(Contract::getType, type)
			.eq(Contract::getBizType, locale)
			.eq(Contract::getStatus, ConstantType.open_or_close.type_1)
			.eq(Contract::getDeleted, 0)
			.last(SysConstant.ONE_LIMIT)
			.one();

		return ResultPista.data(contract);
	}


//	/**
//	 * 查询服务器现在的时间
//	 * @return
//	 */
//	@ApiOperation(value = "查询服务器现在的时间")
//	@GetMapping(value = "/getCurrentDate")
//	public ResultPista<Date> getCurrentDate() {
//		return ResultPista.data(new Date());
//	}
}
