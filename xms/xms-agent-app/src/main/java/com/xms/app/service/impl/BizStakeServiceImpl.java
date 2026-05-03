package com.xms.app.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.*;
import com.xms.app.entity.resp.CreateStakeOrderResp;
import com.xms.app.entity.vo.CreateStakeOrderVo;
import com.xms.dao.entity.vo.StakeOrderProductSnapshotVo;
import com.xms.app.service.BizCommonService;
import com.xms.app.service.BizStakeService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.*;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.UserInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;

/**
 * 质押相关业务
 *
 * @author xms
 * @date 2023/6/12
 */
@Service
@Slf4j
public class BizStakeServiceImpl implements BizStakeService {
	@Autowired
	private UserInfoService userInfoService;


	@Autowired
	private IUserAddressService userAddressServiceImpl;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private IUserMoneyService userMoneyService;

	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;

	@Autowired
	private UserWalletService userWalletService;

	@Autowired
	private BizCommonService bizCommonService;

	@Autowired
	private ISysParaService sysParaService;

	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private IStakeProductService stakeProductService;

	@Autowired
	private IStakeOrderService stakeOrderService;

	@Autowired
	private IStakeReleaseBucketService stakeReleaseBucketService;

	@Value("${lq.md5Key}")
	private String md5Key;

	@Autowired
	private IDiyStoreProductService diyStoreProductService;

	@Autowired
	private IDiyStoreProductAttrValueService diyStoreProductAttrValueService;
	@Autowired
	private IDiyStoreProductAttrService diyStoreProductAttrService;
	@Autowired
	private IDiyStoreProductRuleService diyStoreProductRuleService;


	/**
	 * 发货订单列表
	 *
	 * @param bizType(不传递查询所有的) 0:待发货,1:已发货
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<MyProductOrderDto> myProductOrderList(Integer bizType) {
		List<MyProductOrderDto> result = stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(StakeOrder::getHashFirstShipOrder, 1)
			.eq(bizType != null, StakeOrder::getShipStatus, bizType)
			.list()
			.stream().map(record -> {
				MyProductOrderDto dto = new MyProductOrderDto();
				dto.setProductSnapshot(record.getProductSnapshot());
				dto.setShipStatus(record.getShipStatus());
				dto.setShipTime(record.getShipTime());
				dto.setReceiverInfo(record.getReceiverInfo());
				dto.setShipCompany(record.getShipCompany());
				dto.setShipNo(record.getShipNo());
				dto.setNum(record.getNum());
				dto.setPayTime(record.getPayTime());
				dto.setOrderNo(record.getOrderNo());
				//质押金额
				dto.setStakeUsdtAmount(record.getStakeUsdtAmount());
				return dto;
			}).collect(Collectors.toList());
		return result;
	}

	@Override
	public DiyProductDetailDto diyProductDetail(Long productId) {
		if (productId == null || productId <= 0) {
			return new DiyProductDetailDto();
		}
		DiyStoreProduct product = diyStoreProductService.lambdaQuery()
			.eq(DiyStoreProduct::getId, productId)
			.eq(DiyStoreProduct::getIsEnabled, 1)
			.one();
		if (product == null) {
			throw new ServiceException(ResponseCode.CODE_1266);
		}

/*		DiyProductDetailDto productDetailDto = xmsRedis.get(RedisConstant.DbConstant.DIY_PRODUCT_DETAIL + product.getId(), () ->{
			DiyProductDetailDto result = new DiyProductDetailDto();
			BeanUtil.copyProperties(product, result);
			List<DiyStoreProductAttr> attrList = diyStoreProductAttrService.lambdaQuery()
				.eq(DiyStoreProductAttr::getProductId, product.getId())
				.orderByAsc(DiyStoreProductAttr::getId)
				.list();
			List<DiyStoreProductAttrValue> skuDbList = diyStoreProductAttrValueService.lambdaQuery()
				.eq(DiyStoreProductAttrValue::getProductId, product.getId())
				.orderByAsc(DiyStoreProductAttrValue::getId)
				.list();

			Map<String, DiyStoreProductRule> ruleNameMap = buildRuleNameMap(attrList);

			List<DiyProductDetailDto.SpecItem> specList = new ArrayList<>();
			for (DiyStoreProductAttr attr : attrList) {
				DiyProductDetailDto.SpecItem specItem = new DiyProductDetailDto.SpecItem();
				specItem.setNameZh(attr.getAttrName());
				DiyStoreProductRule rule = ruleNameMap.get(attr.getAttrName());
				specItem.setNameEn(rule == null || StrUtil.isBlank(rule.getRuleNameEn()) ? attr.getAttrName() : rule.getRuleNameEn());

				List<String> valuesZh = splitCsv(attr.getAttrValues());
				List<String> valuesEn = buildValuesEn(valuesZh, rule);
				specItem.setValuesZh(valuesZh);
				specItem.setValuesEn(valuesEn);
				specList.add(specItem);
			}
			result.setSpecList(specList);

			List<DiyProductDetailDto.SkuItem> skuList = new ArrayList<>();
			for (DiyStoreProductAttrValue sku : skuDbList) {
				DiyProductDetailDto.SkuItem skuItem = new DiyProductDetailDto.SkuItem();
				skuItem.setCodeUnique(sku.getCodeUnique());
				skuItem.setSku(sku.getSku());
				skuItem.setPrice(sku.getPrice());
				skuItem.setImage(sku.getImage());
				skuItem.setImageEn(sku.getImageEn());
				skuItem.setStock(sku.getStock());
				skuItem.setSales(sku.getSales());

				List<String> valuesZh = splitSkuValues(sku.getSku());
				List<String> valuesEn = new ArrayList<>();
				for (int i = 0; i < valuesZh.size(); i++) {
					String valueZh = valuesZh.get(i);
					if (i >= specList.size()) {
						valuesEn.add(valueZh);
						continue;
					}
					DiyProductDetailDto.SpecItem specItem = specList.get(i);
					valuesEn.add(mapToEnValue(specItem, valueZh));
				}
				skuItem.setValuesZh(valuesZh);
				skuItem.setValuesEn(valuesEn);
				skuList.add(skuItem);
			}
			result.setSkuList(skuList);
			if (CollectionUtil.isNotEmpty(skuList)) {
				result.setDefaultCodeUnique(skuList.get(0).getCodeUnique());
			}
			return result;
		});*/
		DiyProductDetailDto result = new DiyProductDetailDto();
		BeanUtil.copyProperties(product, result);
		List<DiyStoreProductAttr> attrList = diyStoreProductAttrService.lambdaQuery()
			.eq(DiyStoreProductAttr::getProductId, product.getId())
			.orderByAsc(DiyStoreProductAttr::getId)
			.list();
		List<DiyStoreProductAttrValue> skuDbList = diyStoreProductAttrValueService.lambdaQuery()
			.eq(DiyStoreProductAttrValue::getProductId, product.getId())
			.orderByAsc(DiyStoreProductAttrValue::getId)
			.list();

		Map<String, DiyStoreProductRule> ruleNameMap = buildRuleNameMap(attrList);

		List<DiyProductDetailDto.SpecItem> specList = new ArrayList<>();
		for (DiyStoreProductAttr attr : attrList) {
			DiyProductDetailDto.SpecItem specItem = new DiyProductDetailDto.SpecItem();
			specItem.setNameZh(attr.getAttrName());
			DiyStoreProductRule rule = ruleNameMap.get(attr.getAttrName());
			specItem.setNameEn(rule == null || StrUtil.isBlank(rule.getRuleNameEn()) ? attr.getAttrName() : rule.getRuleNameEn());

			List<String> valuesZh = splitCsv(attr.getAttrValues());
			List<String> valuesEn = buildValuesEn(valuesZh, rule);
			specItem.setValuesZh(valuesZh);
			specItem.setValuesEn(valuesEn);
			specList.add(specItem);
		}
		result.setSpecList(specList);

		List<DiyProductDetailDto.SkuItem> skuList = new ArrayList<>();
		for (DiyStoreProductAttrValue sku : skuDbList) {
			DiyProductDetailDto.SkuItem skuItem = new DiyProductDetailDto.SkuItem();
			skuItem.setCodeUnique(sku.getCodeUnique());
			skuItem.setSku(sku.getSku());
			skuItem.setPrice(sku.getPrice());
			skuItem.setImage(sku.getImage());
			skuItem.setImageEn(sku.getImageEn());
			skuItem.setStock(sku.getStock());
			skuItem.setSales(sku.getSales());

			List<String> valuesZh = splitSkuValues(sku.getSku());
			List<String> valuesEn = new ArrayList<>();
			for (int i = 0; i < valuesZh.size(); i++) {
				String valueZh = valuesZh.get(i);
				if (i >= specList.size()) {
					valuesEn.add(convertDefaultSpecToEn(valueZh));
					continue;
				}
				DiyProductDetailDto.SpecItem specItem = specList.get(i);
				valuesEn.add(mapToEnValue(specItem, valueZh));
			}
			skuItem.setValuesZh(valuesZh);
			skuItem.setValuesEn(valuesEn);
			skuList.add(skuItem);
		}
		result.setSkuList(skuList);
		if (CollectionUtil.isNotEmpty(skuList)) {
			result.setDefaultCodeUnique(skuList.get(0).getCodeUnique());
		}
		return result;
	}

	private Map<String, DiyStoreProductRule> buildRuleNameMap(List<DiyStoreProductAttr> attrList) {
		if (CollectionUtil.isEmpty(attrList)) {
			return Collections.emptyMap();
		}
		Set<String> attrNameSet = attrList.stream()
			.map(DiyStoreProductAttr::getAttrName)
			.filter(StrUtil::isNotBlank)
			.collect(Collectors.toSet());
		if (CollectionUtil.isEmpty(attrNameSet)) {
			return Collections.emptyMap();
		}
		List<DiyStoreProductRule> ruleList = diyStoreProductRuleService.lambdaQuery()
			.in(DiyStoreProductRule::getRuleName, attrNameSet)
			.list();
		if (CollectionUtil.isEmpty(ruleList)) {
			return Collections.emptyMap();
		}
		return ruleList.stream().collect(Collectors.toMap(
			DiyStoreProductRule::getRuleName,
			item -> item,
			(a, b) -> a
		));
	}

	private List<String> splitCsv(String csv) {
		if (StrUtil.isBlank(csv)) {
			return new ArrayList<>();
		}
		return Arrays.stream(csv.split(","))
			.map(String::trim)
			.filter(StrUtil::isNotBlank)
			.collect(Collectors.toList());
	}

	private List<String> buildValuesEn(List<String> valuesZh, DiyStoreProductRule rule) {
		if (CollectionUtil.isEmpty(valuesZh)) {
			return new ArrayList<>();
		}
		List<String> valuesEnFromRule = rule == null ? new ArrayList<>() : parseRuleValues(rule.getRuleValueEn());
		List<String> valuesEn = new ArrayList<>();
		for (int i = 0; i < valuesZh.size(); i++) {
			String zhValue = valuesZh.get(i);
			String enValue = (valuesEnFromRule.size() > i && StrUtil.isNotBlank(valuesEnFromRule.get(i)))
				? valuesEnFromRule.get(i)
				: convertDefaultSpecToEn(zhValue);
			valuesEn.add(enValue);
		}
		return valuesEn;
	}

	private String mapToEnValue(DiyProductDetailDto.SpecItem specItem, String valueZh) {
		if (specItem == null || CollectionUtil.isEmpty(specItem.getValuesZh())) {
			return convertDefaultSpecToEn(valueZh);
		}
		for (int i = 0; i < specItem.getValuesZh().size(); i++) {
			if (StrUtil.equals(specItem.getValuesZh().get(i), valueZh)) {
				if (specItem.getValuesEn().size() > i && StrUtil.isNotBlank(specItem.getValuesEn().get(i))) {
					return specItem.getValuesEn().get(i);
				}
				return convertDefaultSpecToEn(valueZh);
			}
		}
		return convertDefaultSpecToEn(valueZh);
	}

	private String convertDefaultSpecToEn(String zhValue) {
		if (StrUtil.equalsAny(zhValue, "默认规格", "默认")) {
			return "Default Spec";
		}
		return zhValue;
	}

	private String convertSpecNameToEn(String zhName) {
		if (StrUtil.equals(zhName, "规格")) {
			return "Spec";
		}
		return zhName;
	}

	@Override
	public List<DiyProductListDto> diyProductList() {
		List<DiyProductListDto> result = xmsRedis.get(RedisConstant.DbConstant.DIY_PRODUCT_LIST, () -> diyStoreProductService.lambdaQuery()
			.eq(DiyStoreProduct::getIsEnabled, 1)
			.orderByAsc(DiyStoreProduct::getSort)
			.list()
			.stream().map(record -> {
				DiyProductListDto diyProductListDto = new DiyProductListDto();
				BeanUtil.copyProperties(record, diyProductListDto);
				return diyProductListDto;
			}).collect(Collectors.toList()));
		return result;
	}

	@Override
	public ResultPista<String> stakeOrderCallback(StakeOrderBo req) {
		log.info("质押订单回调 req:{}", req);
		if (!StrUtil.isBlank(req.getHash())) {
			if (req.getHash().length() > 255) {
				throw new ServiceException("hash长度不能超过255");
			}
		}

		// 将 RechargeCallbackBo 对象转换为 Map
		Map<String, Object> map = cn.hutool.core.bean.BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("验签失败");
				return ResultPista.fail("验签失败");
			}
		}


		StakeOrder queryStakeOrder = stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getOrderNo, req.getOrderNo())
			.one();
		if (queryStakeOrder == null) {
			throw new ServiceException("质押订单不存在");
		}
		if (req.getAmount().compareTo(queryStakeOrder.getStakeUsdtAmount()) < 0) {
			throw new ServiceException("质押金额不一致");
		}

		if (queryStakeOrder.getStatus().equals(0) || queryStakeOrder.getStatus().equals(3)) {
			SpringUtils.getBean(BizStakeServiceImpl.class).doStakeOrderCallback(req, queryStakeOrder, queryStakeOrder.getUserId());
		}


		return ResultPista.data("success");
	}

	/**
	 * 购买矿机回调
	 *
	 * @param req
	 * @param queryStakeOrder
	 * @param userId
	 */
	@RedisLock(value = RedisConstant.LockConstant.XMS_BUY_MINING_CALL_BACK, param = "#userId")
	@Transactional(rollbackFor = Exception.class)
	public void doStakeOrderCallback(StakeOrderBo req, StakeOrder queryStakeOrder, Long userId) {
		//查询用户
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();

		boolean update = stakeOrderService.lambdaUpdate()
			.eq(StakeOrder::getId, queryStakeOrder.getId())
			.eq(StakeOrder::getStatus, queryStakeOrder.getStatus())
			.set(StakeOrder::getStatus, 1)
			.set(StakeOrder::getPayTime, new Date())
			.set(StakeOrder::getPayHash, req.getHash())
			.set(StakeOrder::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("更新质押订单失败");
		}


		StakeOrderProductSnapshotVo stakeOrderProductSnapshotVo = JSON.parseObject(queryStakeOrder.getProductSnapshot(), StakeOrderProductSnapshotVo.class);
		//更新商品销量
		diyStoreProductService.lambdaUpdate()
			.eq(DiyStoreProduct::getId, Long.valueOf(stakeOrderProductSnapshotVo.getProductId()))
			.setSql("sales = sales + " + queryStakeOrder.getNum())
			.update();

		update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userInfo.getUserId())
			.setSql("performance = performance + " + queryStakeOrder.getStakeUsdtAmount())
			.update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = umbrella_performance + " + queryStakeOrder.getStakeUsdtAmount())
				.update();
			if (!update) {
				throw new ServiceException(ResponseCode.CODE_1002);
			}

			//计算小区业绩和大区业绩
			calculateCommunityPerformance(parentIds);
		}

		//后续费的分红信息、和等级计算
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
				OrderMsgDO orderMsgDO = new OrderMsgDO();
				orderMsgDO.setId(queryStakeOrder.getId());
				orderMsgDO.setBizType(1);
				orderMsgDOList.add(orderMsgDO);
				asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
			}
		});
	}

	/**
	 * 管理后台和app计算都有计算等级的地方
	 * 计算小区业绩+大区业绩
	 * @param parentIds
	 */
	public void calculateCommunityPerformance(List<Long> parentIds) {
		// 小区业绩（对所有上级计算：去掉最大直推线）
		if (CollectionUtil.isNotEmpty(parentIds)) {
			for (Long parentId : parentIds) {
				List<UserInfo> children = userInfoService.lambdaQuery()
					.eq(UserInfo::getInviteUserId, parentId)
					.select(UserInfo::getUserId, UserInfo::getUmbrellaPerformance,
						UserInfo::getPerformance)
					.list();
				BigDecimal maxLegPerformance = UserInfoServiceImpl.getMaxTeamPerformance(children);
				if (CollectionUtil.isEmpty(children) || children.size() <= 1) {
					//更新小区业绩、和大区业绩
					userInfoService.lambdaUpdate()
						.eq(UserInfo::getUserId, parentId)
						.set(UserInfo::getCommunityPerformance, BigDecimal.ZERO)
						.update();
					continue;
				}
				BigDecimal totalChildPerformance = BigDecimal.ZERO;
				BigDecimal maxChildPerformance = BigDecimal.ZERO;
				for (UserInfo child : children) {
					BigDecimal childUmbrella = child.getUmbrellaPerformance();
					BigDecimal performance = child.getPerformance();
					childUmbrella = childUmbrella.add(performance);

					totalChildPerformance = totalChildPerformance.add(childUmbrella);
					if (childUmbrella.compareTo(maxChildPerformance) > 0) {
						maxChildPerformance = childUmbrella;
					}
				}
				BigDecimal communityPerformance = totalChildPerformance.subtract(maxChildPerformance);
				if (communityPerformance.compareTo(BigDecimal.ZERO) < 0) {
					communityPerformance = BigDecimal.ZERO;
				}

				//更新小区业绩和大区业绩
				userInfoService.lambdaUpdate()
					.eq(UserInfo::getUserId, parentId)
					.set(UserInfo::getCommunityPerformance, communityPerformance)
					.update();
			}
		}
	}

	/**
	 * 质押订单列表
	 *
	 * @param lastId
	 * @param status 1:产出中,2:已出局
	 * @return
	 */
	@Override
	public List<MyStakeInfoListDto> destroyOrderList(Long lastId, Integer status) {
		List<StakeOrder> packageOrderList = stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(Func.isNotEmpty(status), StakeOrder::getStatus, status)
			.lt(Func.isNotEmpty(lastId), StakeOrder::getId, lastId)
			.orderByDesc(StakeOrder::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		if (CollectionUtil.isEmpty(packageOrderList)) {
			return new ArrayList<>();
		}
		StakeProduct stakeProduct = stakeProductService.lambdaQuery()
			.last("limit 1")
			.select(StakeProduct::getStaticRatio)
			.one();

		//查询奖金记录该订单贡献了多少钱
		List<MyStakeInfoListDto> result = packageOrderList.stream().map(item -> {
			MyStakeInfoListDto dto = new MyStakeInfoListDto();
			BeanUtil.copyProperties(item, dto);
			dto.setDayRatio(stakeProduct.getStaticRatio());
			//如果是已经出局的订单并且更新时间不是今天说明肯定没有今日收益了
			if (Integer.valueOf(2).equals(status)
				&& (!DateUtil.isSameDay(item.getUpdateTime(), new Date()))) {
				dto.setTodayReward(BigDecimal.ZERO);
				return dto;
			}

			BigDecimal todayReward = rewardRecordService.lambdaQuery()
				.eq(RewardRecord::getUserId, item.getUserId())
				.eq(RewardRecord::getSourceOrderCode, item.getOrderNo())
				.eq(RewardRecord::getSourceType, 6)
				.select(RewardRecord::getAmount)
				.apply("DATE(create_time) = CURDATE()")
				.list().stream().map(RewardRecord::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			dto.setTodayReward(todayReward);
			return dto;
		}).collect(Collectors.toList());
		return result;
	}

	/**
	 * 锁仓订单列表
	 *
	 * @param lastId id
	 * @return
	 */
	@Override
	public List<MyReleaseBucketListDto> myReleaseBucketList(Long lastId) {
		List<StakeReleaseBucket> releaseBucketList = stakeReleaseBucketService.lambdaQuery()
			.eq(StakeReleaseBucket::getUserId, SecurityUtils.getFrontUserId())
			.lt(Func.isNotEmpty(lastId), StakeReleaseBucket::getId, lastId)
			.orderByDesc(StakeReleaseBucket::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		if (CollectionUtil.isEmpty(releaseBucketList)) {
			return new ArrayList<>();
		}
		List<MyReleaseBucketListDto> result = releaseBucketList.stream().map(item -> {
			MyReleaseBucketListDto dto = new MyReleaseBucketListDto();
			BeanUtil.copyProperties(item, dto);
			return dto;
		}).collect(Collectors.toList());
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#userId")
	public ResultPista<CreateStakeOrderResp> createStakeOrder(CreateStakeOrderVo req, Long userId) {
		if (req.getNum() <= 0) {
			throw new ServiceException(ResponseCode.CODE_1267);
		}
		if (req.getNum() > 1) {
			throw new ServiceException(ResponseCode.CODE_1003);
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		req.setUserAmount(req.getUserAmount().setScale(2, ConstantStatic.roundingModeNew));
		//校验是否传了地址id
		String userAddressJsonStr = null;

		//不保留小数
		//验签，随机数
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);

		StakeProduct stakeProduct = stakeProductService.lambdaQuery()
			.eq(StakeProduct::getIsEnabled, 1)
			.last("limit 1")
			.one();
		if (stakeProduct == null) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		//查询该产品是否超过一天限购
		Long unPayOrders = stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getUserId, userId)
			.eq(StakeOrder::getStatus, 0)
			.apply("create_time >= CURDATE()")
			.count();
		if (unPayOrders > 10) {
			throw new ServiceException(ResponseCode.CODE_1263);
		}
		DiyStoreProduct product = diyStoreProductService.lambdaQuery()
			.eq(DiyStoreProduct::getId, req.getProductId())
			.one();
		if (product == null || product.getIsEnabled() == 0) {
			throw new ServiceException(ResponseCode.CODE_1264);
		}
		//获取对应的sku
		DiyStoreProductAttrValue productAttrValue = diyStoreProductAttrValueService.lambdaQuery()
			.eq(DiyStoreProductAttrValue::getProductId, req.getProductId())
			.eq(DiyStoreProductAttrValue::getCodeUnique, req.getCodeUnique())
			.one();
		if (productAttrValue == null) {
			throw new ServiceException(ResponseCode.CODE_1264);
		}
		if (req.getUserAmount().compareTo(productAttrValue.getPrice()) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}


		//创建质押对象
		String orderNo = IDUtils.getSnowflake().nextIdStr();
		StakeOrder stakeOrder = new StakeOrder();
		stakeOrder.setUserId(userId);
		stakeOrder.setBizStatus1(0);
		stakeOrder.setOrderNo(orderNo);
		BigDecimal bigNum = new BigDecimal(req.getNum());
		BigDecimal stakeUsdtAmount = productAttrValue.getPrice().multiply(bigNum)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		stakeOrder.setStakeUsdtAmount(stakeUsdtAmount);
		stakeOrder.setStatus(0);
		stakeOrder.setNum(req.getNum());
		BigDecimal allOutAmount = stakeUsdtAmount.multiply(stakeProduct.getExitMultiplier())
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		stakeOrder.setAllOutAmount(allOutAmount);
		stakeOrder.setRemainingOutAmount(allOutAmount);

		//默认都是没有发货状态(在支付那一刻来判断)
		stakeOrder.setHashFirstShipOrder(0);
		stakeOrder.setShipStatus(0);
		stakeOrder.setReceiverInfo(userAddressJsonStr);
		// 设置商品快照（中英扁平字段）
		String productSnapshot = buildProductSnapshot(product, productAttrValue);
		stakeOrder.setProductSnapshot(productSnapshot);

		//设置商品快照、以及收货id
		stakeOrder.setCreateTime(new Date());
		boolean save = stakeOrderService.save(stakeOrder);
		if (!save) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		CreateStakeOrderResp resp = new CreateStakeOrderResp();
		resp.setOrderNo(orderNo);
		resp.setStakeUsdtAmount(stakeOrder.getStakeUsdtAmount());
		return ResultPista.data(resp);
	}

	private String buildProductSnapshot(DiyStoreProduct product, DiyStoreProductAttrValue productAttrValue) {
		StakeOrderProductSnapshotVo snapshotVo = new StakeOrderProductSnapshotVo();
		snapshotVo.setProductId(product.getId());
		snapshotVo.setProductCode(product.getProductCode());
		snapshotVo.setSpecType(product.getSpecType());
		snapshotVo.setDealPrice(productAttrValue.getPrice());
		snapshotVo.setCurrency("USDT");

		snapshotVo.setProductNameZh(product.getProductName());
		snapshotVo.setProductNameEn(product.getProductNameEn());
		snapshotVo.setProductCoverImageZh(product.getCoverImage());
		snapshotVo.setProductCoverImageEn(product.getCoverImageEn());
		snapshotVo.setProductSliderImagesZh(product.getSliderImage());
		snapshotVo.setProductSliderImagesEn(product.getSliderImageEn());
		snapshotVo.setProductDetailImagesZh(product.getDetailImage());
		snapshotVo.setProductDetailImagesEn(product.getDetailImageEn());

		snapshotVo.setSkuId(productAttrValue.getId());
		snapshotVo.setSkuCodeUnique(productAttrValue.getCodeUnique());
		snapshotVo.setSkuImageZh(productAttrValue.getImage());
		snapshotVo.setSkuImageEn(productAttrValue.getImageEn());

		String sku = productAttrValue.getSku();
		if (StrUtil.isBlank(sku)) {
			snapshotVo.setSkuTextZh("");
			snapshotVo.setSkuTextEn("");
			return JSON.toJSONString(snapshotVo);
		}

		List<DiyStoreProductAttr> attrList = diyStoreProductAttrService.lambdaQuery()
			.eq(DiyStoreProductAttr::getProductId, product.getId())
			.list();
		Set<String> attrNameSet = attrList.stream()
			.map(DiyStoreProductAttr::getAttrName)
			.collect(Collectors.toSet());

		List<DiyStoreProductRule> candidateRules = CollectionUtil.isEmpty(attrNameSet)
			? new ArrayList<>()
			: diyStoreProductRuleService.lambdaQuery()
			.in(DiyStoreProductRule::getRuleName, attrNameSet)
			.list();
		List<RuleSpec> ruleSpecs = candidateRules.stream()
			.map(this::toRuleSpec)
			.collect(Collectors.toList());

		List<String> skuValues = splitSkuValues(sku);
		List<String> zhPairs = new ArrayList<>();
		List<String> enPairs = new ArrayList<>();
		for (String value : skuValues) {
			MatchedRule matchedRule = matchRuleValue(value, ruleSpecs);
			if (matchedRule == null) {
				zhPairs.add(value);
				enPairs.add(convertDefaultSpecToEn(value));
				continue;
			}
			RuleSpec rule = matchedRule.getRule();
			int idx = matchedRule.getIndex();
			String zhName = StrUtil.isBlank(rule.getRuleNameZh()) ? "规格" : rule.getRuleNameZh();
			String enName = StrUtil.isBlank(rule.getRuleNameEn()) ? convertSpecNameToEn(zhName) : rule.getRuleNameEn();
			String enValue = (rule.getValuesEn().size() > idx && StrUtil.isNotBlank(rule.getValuesEn().get(idx)))
				? rule.getValuesEn().get(idx)
				: convertDefaultSpecToEn(value);
			zhPairs.add(zhName + ":" + value);
			enPairs.add(enName + ":" + enValue);
		}
		snapshotVo.setSkuTextZh(StrUtil.join(" | ", zhPairs));
		snapshotVo.setSkuTextEn(StrUtil.join(" | ", enPairs));
		return JSON.toJSONString(snapshotVo);
	}

	private List<String> splitSkuValues(String sku) {
		return StrUtil.split(sku, '|')
			.stream()
			.map(String::trim)
			.filter(StrUtil::isNotBlank)
			.collect(Collectors.toList());
	}

	private RuleSpec toRuleSpec(DiyStoreProductRule rule) {
		RuleSpec spec = new RuleSpec();
		spec.setRuleNameZh(rule.getRuleName());
		spec.setRuleNameEn(rule.getRuleNameEn());
		spec.setValuesZh(parseRuleValues(rule.getRuleValue()));
		spec.setValuesEn(parseRuleValues(rule.getRuleValueEn()));
		return spec;
	}

	private MatchedRule matchRuleValue(String value, List<RuleSpec> ruleSpecs) {
		for (RuleSpec rule : ruleSpecs) {
			List<String> valuesZh = rule.getValuesZh();
			for (int i = 0; i < valuesZh.size(); i++) {
				if (StrUtil.equals(valuesZh.get(i), value)) {
					return new MatchedRule(rule, i);
				}
			}
		}
		return null;
	}

	private List<String> parseRuleValues(String ruleValueJson) {
		if (!JSONUtil.isTypeJSON(ruleValueJson)) {
			return new ArrayList<>();
		}
		JSONArray array = JSONUtil.parseArray(ruleValueJson);
		if (CollectionUtil.isEmpty(array)) {
			return new ArrayList<>();
		}
		JSONObject first = array.getJSONObject(0);
		if (first == null) {
			return new ArrayList<>();
		}
		JSONArray detail = first.getJSONArray("detail");
		if (CollectionUtil.isEmpty(detail)) {
			return new ArrayList<>();
		}
		return detail.stream()
			.map(item -> item == null ? "" : item.toString().trim())
			.filter(StrUtil::isNotBlank)
			.collect(Collectors.toList());
	}

	private static class RuleSpec {
		private String ruleNameZh;
		private String ruleNameEn;
		private List<String> valuesZh = new ArrayList<>();
		private List<String> valuesEn = new ArrayList<>();

		public String getRuleNameZh() {
			return ruleNameZh;
		}

		public void setRuleNameZh(String ruleNameZh) {
			this.ruleNameZh = ruleNameZh;
		}

		public String getRuleNameEn() {
			return ruleNameEn;
		}

		public void setRuleNameEn(String ruleNameEn) {
			this.ruleNameEn = ruleNameEn;
		}

		public List<String> getValuesZh() {
			return valuesZh;
		}

		public void setValuesZh(List<String> valuesZh) {
			this.valuesZh = valuesZh;
		}

		public List<String> getValuesEn() {
			return valuesEn;
		}

		public void setValuesEn(List<String> valuesEn) {
			this.valuesEn = valuesEn;
		}
	}

	private static class MatchedRule {
		private final RuleSpec rule;
		private final int index;

		private MatchedRule(RuleSpec rule, int index) {
			this.rule = rule;
			this.index = index;
		}

		public RuleSpec getRule() {
			return rule;
		}

		public int getIndex() {
			return index;
		}
	}

	/**
	 * 获取质押信息
	 *
	 * @return
	 */
	@Override
	public StakeInfoDTO stakeInfo() {
		StakeProduct stakeProduct = stakeProductService.lambdaQuery()
			.eq(StakeProduct::getIsEnabled, 1)
			.last("limit 1")
			.one();
		StakeInfoDTO stakeInfoDTO = new StakeInfoDTO();
		if (stakeProduct == null) {
			return stakeInfoDTO;
		}
		stakeInfoDTO.setStakeUnitAmountMin(stakeProduct.getStakeUnitAmountMin());
		stakeInfoDTO.setMaxStakeAmount(stakeProduct.getMaxStakeAmount());
		stakeInfoDTO.setStaticRatio(stakeProduct.getStaticRatio());
		stakeInfoDTO.setExitMultiplier(stakeProduct.getExitMultiplier());
		return stakeInfoDTO;
	}
}
