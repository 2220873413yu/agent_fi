package com.xms.web.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.config.RedissonTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.dao.domain.DiyStoreProduct;
import com.xms.dao.domain.DiyStoreProductAttr;
import com.xms.dao.domain.DiyStoreProductAttrValue;
import com.xms.dao.domain.DiyStoreProductRule;
import com.xms.dao.entity.bo.BizProductDetailBo;
import com.xms.dao.entity.req.BizProductDetailReq;
import com.xms.dao.mapper.DiyStoreProductAttrMapper;
import com.xms.dao.mapper.DiyStoreProductAttrValueMapper;
import com.xms.dao.service.IDiyStoreProductAttrService;
import com.xms.dao.service.IDiyStoreProductAttrValueService;
import com.xms.dao.service.IDiyStoreProductRuleService;
import com.xms.dao.service.IDiyStoreProductService;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.web.service.BizStoreProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BizStoreProductServiceImpl implements BizStoreProductService {
	private static final Logger log = LoggerFactory.getLogger(BizStoreProductServiceImpl.class);

	private static final String SINGLE_SPEC_NAME = "规格";
	private static final String SINGLE_SPEC_VALUE = "默认规格";

	@Autowired
	private IDiyStoreProductService diyStoreProductService;
	@Autowired
	private IDiyStoreProductAttrService diyStoreProductAttrService;
	@Autowired
	private IDiyStoreProductAttrValueService diyStoreProductAttrValueService;
	@Autowired
	private IDiyStoreProductRuleService diyStoreProductRuleService;
	@Autowired
	private DiyStoreProductAttrMapper diyStoreProductAttrMapper;
	@Autowired
	private DiyStoreProductAttrValueMapper diyStoreProductAttrValueMapper;


	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private RedissonTemplate redissonTemplate;

	@Override
	public BizProductDetailBo getById(String id) {
		DiyStoreProduct product = diyStoreProductService.getById(id);
		BizProductDetailBo bizProductDetailBo = new BizProductDetailBo();
		if (product == null) {
			return bizProductDetailBo;
		}
		bizProductDetailBo.setId(product.getId());
		bizProductDetailBo.setProductName(product.getProductName());
		bizProductDetailBo.setProductNameEn(product.getProductNameEn());
		bizProductDetailBo.setProductCode(product.getProductCode());
		bizProductDetailBo.setCoverImage(product.getCoverImage());
		bizProductDetailBo.setCoverImageEn(product.getCoverImageEn());
		bizProductDetailBo.setSliderImage(product.getSliderImage());
		bizProductDetailBo.setSliderImageEn(product.getSliderImageEn());
		bizProductDetailBo.setDetailImage(product.getDetailImage());
		bizProductDetailBo.setDetailImageEn(product.getDetailImageEn());
		bizProductDetailBo.setPrice(product.getPrice());
		bizProductDetailBo.setSpecType(product.getSpecType() == null ? 0 : product.getSpecType());
		bizProductDetailBo.setSales(product.getSales());
		bizProductDetailBo.setStock(product.getStock());
		bizProductDetailBo.setIsEnabled(product.getIsEnabled());
		bizProductDetailBo.setSort(product.getSort());
		bizProductDetailBo.setRemark(product.getRemark());

		List<DiyStoreProductAttr> attrList = diyStoreProductAttrService.lambdaQuery()
			.eq(DiyStoreProductAttr::getProductId, product.getId())
			.list();
		List<DiyStoreProductAttrValue> attrValueList = diyStoreProductAttrValueService.lambdaQuery()
			.eq(DiyStoreProductAttrValue::getProductId, product.getId())
			.list();

		if (!attrValueList.isEmpty()) {
			List<BizProductDetailReq.AttrValueItem> valueItems = attrValueList.stream().map(item -> {
				BizProductDetailReq.AttrValueItem valueItem = new BizProductDetailReq.AttrValueItem();
				valueItem.setId(item.getId());
				valueItem.setSku(item.getSku());
				valueItem.setStock(item.getStock());
				valueItem.setSales(item.getSales());
				valueItem.setPrice(item.getPrice());
				valueItem.setImage(item.getImage());
				valueItem.setImageEn(item.getImageEn());
				valueItem.setCodeUnique(item.getCodeUnique());
				return valueItem;
			}).collect(Collectors.toList());
			bizProductDetailBo.setAttrValueList(valueItems);
			BizProductDetailReq.AttrValueItem first = valueItems.get(0);
			bizProductDetailBo.setSingleSpecPrice(first.getPrice());
			bizProductDetailBo.setSingleSpecImage(first.getImage());
			bizProductDetailBo.setSingleSpecImageEn(first.getImageEn());
		}

		// 回填规格模板ID（多规格场景）
		if (!attrList.isEmpty()) {
			Map<String, String> attrSignatureMap = attrList.stream()
				.collect(Collectors.toMap(
					DiyStoreProductAttr::getAttrName,
					item -> normalizeCsv(item.getAttrValues()),
					(a, b) -> a
				));
			if (!attrSignatureMap.isEmpty()) {
				List<DiyStoreProductRule> ruleList = diyStoreProductRuleService.list();
				List<Long> selectedRuleIds = new ArrayList<>();
				for (DiyStoreProductRule rule : ruleList) {
					String ruleName = rule.getRuleName();
					List<String> values = parseRuleValues(rule.getRuleValue());
					if (ruleName == null || values.isEmpty()) {
						continue;
					}
					String attrCsv = attrSignatureMap.get(ruleName);
					if (attrCsv != null && attrCsv.equals(normalizeCsv(String.join(",", values)))) {
						selectedRuleIds.add(rule.getId());
					}
				}
				bizProductDetailBo.setSpecAttrIds(selectedRuleIds);
			}
		}
		return bizProductDetailBo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdate(BizProductDetailReq diyStoreProduct) {
		Integer specType = diyStoreProduct.getSpecType() == null ? 0 : diyStoreProduct.getSpecType();

		DiyStoreProduct product = new DiyStoreProduct();
		product.setId(diyStoreProduct.getId());
		product.setProductName(diyStoreProduct.getProductName());
		product.setProductNameEn(diyStoreProduct.getProductNameEn());
		product.setProductCode(diyStoreProduct.getProductCode());
		product.setCoverImage(diyStoreProduct.getCoverImage());
		product.setCoverImageEn(diyStoreProduct.getCoverImageEn());
		product.setSliderImage(diyStoreProduct.getSliderImage());
		product.setSliderImageEn(diyStoreProduct.getSliderImageEn());
		product.setDetailImage(diyStoreProduct.getDetailImage());
		product.setDetailImageEn(diyStoreProduct.getDetailImageEn());
		product.setSpecType(specType);
		product.setIsEnabled(diyStoreProduct.getIsEnabled());
		product.setSort(diyStoreProduct.getSort());
		product.setRemark(diyStoreProduct.getRemark());
		product.setStock(diyStoreProduct.getStock() == null ? 0 : diyStoreProduct.getStock());
		product.setSales(diyStoreProduct.getSales() == null ? "0" : diyStoreProduct.getSales());

		BigDecimal defaultPrice = diyStoreProduct.getPrice();
		if (defaultPrice == null && diyStoreProduct.getSingleSpecPrice() != null) {
			defaultPrice = diyStoreProduct.getSingleSpecPrice();
		}
		if (defaultPrice == null && diyStoreProduct.getAttrValueList() != null && !diyStoreProduct.getAttrValueList().isEmpty()) {
			defaultPrice = diyStoreProduct.getAttrValueList().get(0).getPrice();
		}
		product.setPrice(defaultPrice);

		boolean saveResult = diyStoreProductService.saveOrUpdate(product);
		if (!saveResult) {
			return 0;
		}
		String productId = product.getId();
		if (productId == null || productId.isEmpty()) {
			throw new ServiceException("保存商品后未获得商品ID");
		}

		// 先读取旧SKU映射，确保重复保存时已存在SKU沿用旧codeUnique
		Map<String, String> oldSkuCodeUniqueMap = diyStoreProductAttrValueService.lambdaQuery()
			.eq(DiyStoreProductAttrValue::getProductId, productId)
			.list()
			.stream()
			.collect(Collectors.toMap(
				item -> normalizeSku(item.getSku()),
				DiyStoreProductAttrValue::getCodeUnique,
				(a, b) -> a
			));

		// 先物理清空旧明细（逻辑删会保留行，若表上有 product_id+sku 等唯一约束，第二次保存会插入冲突）
		diyStoreProductAttrMapper.deletePhysicalByProductId(productId);
		diyStoreProductAttrValueMapper.deletePhysicalByProductId(productId);

		// 保存商品属性（xms_diy_store_product_attr）
		List<DiyStoreProductAttr> productAttrs = buildProductAttrs(productId, specType, diyStoreProduct);
		if (!productAttrs.isEmpty()) {
			diyStoreProductAttrService.saveBatch(productAttrs);
		}

		// 保存SKU（xms_diy_store_product_attr_value）
		List<DiyStoreProductAttrValue> skuList = buildSkuList(productId, specType, diyStoreProduct, oldSkuCodeUniqueMap);
		if (!skuList.isEmpty()) {
			diyStoreProductAttrValueService.saveBatch(skuList);
		}

		// 读取一次规则模板表（xms_diy_store_product_rule），确保与选中模板关联校验
		if (specType == 1 && diyStoreProduct.getSpecAttrIds() != null && !diyStoreProduct.getSpecAttrIds().isEmpty()) {
			long count = diyStoreProductRuleService.lambdaQuery()
				.in(DiyStoreProductRule::getId, diyStoreProduct.getSpecAttrIds())
				.count();
			if (count != diyStoreProduct.getSpecAttrIds().size()) {
				log.warn("部分规格模板不存在, productId={}, ruleIds={}", productId, diyStoreProduct.getSpecAttrIds());
			}
		}


		//删除商品列表
		String key1 = RedisConstant.DbConstant.DIY_PRODUCT_LIST;
		String key2 =RedisConstant.DbConstant.DIY_PRODUCT_DETAIL+productId;
		redissonTemplate.sendCleanCacheWithDelay(key1+","+key2);

		xmsRedis.del(key1);
		xmsRedis.del(key2);
		//删除商品详情缓存
		return 1;
	}

	private List<DiyStoreProductAttr> buildProductAttrs(String productId, Integer specType, BizProductDetailReq req) {
		List<DiyStoreProductAttr> result = new ArrayList<>();
		if (specType != null && specType == 1) {
			List<BizProductDetailReq.AttrValueItem> reqSkuList = req.getAttrValueList();
			if (CollectionUtil.isNotEmpty(reqSkuList)) {
				List<String> specNames = getSpecNamesByRuleIds(req.getSpecAttrIds());
				Map<Integer, LinkedHashSet<String>> indexValueMap = new LinkedHashMap<>();
				for (BizProductDetailReq.AttrValueItem item : reqSkuList) {
					String normalizedSku = normalizeSku(item.getSku());
					if (isBlank(normalizedSku)) {
						continue;
					}
					String[] values = normalizedSku.split("\\|");
					for (int i = 0; i < values.length; i++) {
						String value = String.valueOf(values[i]).trim();
						if (isBlank(value)) {
							continue;
						}
						indexValueMap.computeIfAbsent(i, k -> new LinkedHashSet<>()).add(value);
					}
				}
				for (Map.Entry<Integer, LinkedHashSet<String>> entry : indexValueMap.entrySet()) {
					List<String> values = new ArrayList<>(entry.getValue());
					if (values.isEmpty()) {
						continue;
					}
					int idx = entry.getKey();
					String attrName = specNames.size() > idx ? specNames.get(idx) : ("规格" + (idx + 1));
					DiyStoreProductAttr attr = new DiyStoreProductAttr();
					attr.setId(null);
					attr.setProductId(productId);
					attr.setAttrName(attrName);
					attr.setAttrValues(String.join(",", values));
					result.add(attr);
				}
				if (!result.isEmpty()) {
					return result;
				}
			}

			List<Long> specAttrIds = req.getSpecAttrIds();
			if (CollectionUtil.isEmpty(specAttrIds)) {
				return result;
			}
			List<DiyStoreProductRule> rules = diyStoreProductRuleService.lambdaQuery()
				.in(DiyStoreProductRule::getId, specAttrIds)
				.list();
			for (DiyStoreProductRule rule : rules) {
				List<String> values = parseRuleValues(rule.getRuleValue());
				DiyStoreProductAttr attr = new DiyStoreProductAttr();
				attr.setId(null);
				attr.setProductId(productId);
				attr.setAttrName(rule.getRuleName());
				attr.setAttrValues(String.join(",", values));
				result.add(attr);
			}
			return result;
		}
		DiyStoreProductAttr attr = new DiyStoreProductAttr();
		attr.setId(null);
		attr.setProductId(productId);
		attr.setAttrName(SINGLE_SPEC_NAME);
		attr.setAttrValues(SINGLE_SPEC_VALUE);
		result.add(attr);
		return result;
	}

	private List<DiyStoreProductAttrValue> buildSkuList(String productId, Integer specType, BizProductDetailReq req, Map<String, String> oldSkuCodeUniqueMap) {
		List<DiyStoreProductAttrValue> skuList = new ArrayList<>();
		List<BizProductDetailReq.AttrValueItem> reqSkuList = req.getAttrValueList();
		if (reqSkuList == null) {
			reqSkuList = Collections.emptyList();
		}
		if (oldSkuCodeUniqueMap == null) {
			oldSkuCodeUniqueMap = Collections.emptyMap();
		}
		if (specType != null && specType == 0 && reqSkuList.isEmpty()) {
			BizProductDetailReq.AttrValueItem item = new BizProductDetailReq.AttrValueItem();
			item.setSku("默认");
			item.setPrice(req.getSingleSpecPrice() != null ? req.getSingleSpecPrice() : req.getPrice());
			item.setImage(req.getSingleSpecImage());
			item.setImageEn(req.getSingleSpecImageEn());
			item.setStock("0");
			item.setSales("0");
			reqSkuList = Collections.singletonList(item);
		}
		Set<String> normalizedSkuSet = new HashSet<>();
		for (BizProductDetailReq.AttrValueItem item : reqSkuList) {
			String normalizedSku = normalizeSku(item.getSku());
			if (isBlank(normalizedSku)) {
				throw new ServiceException("规格值不能为空");
			}
			if (!normalizedSkuSet.add(normalizedSku)) {
				throw new ServiceException("存在重复规格组合: " + normalizedSku);
			}
			DiyStoreProductAttrValue sku = new DiyStoreProductAttrValue();
			sku.setId(null);
			sku.setProductId(productId);
			sku.setSku(normalizedSku);
			sku.setPrice(item.getPrice());
			sku.setImage(item.getImage());
			sku.setImageEn(item.getImageEn());
			sku.setStock(item.getStock() == null ? "0" : item.getStock());
			sku.setSales(item.getSales() == null ? "0" : item.getSales());
			String codeUnique = item.getCodeUnique();
			if (isBlank(codeUnique)) {
				codeUnique = oldSkuCodeUniqueMap.get(normalizedSku);
			}
			if (isBlank(codeUnique)) {
				codeUnique = IDUtils.getSnowflakeStr();
			}
			sku.setCodeUnique(codeUnique);
			skuList.add(sku);
		}
		return skuList;
	}

	private List<String> parseRuleValues(String ruleValue) {
		if (!JSONUtil.isTypeJSON(ruleValue)) {
			return Collections.emptyList();
		}
		JSONArray array = JSONUtil.parseArray(ruleValue);
		if (array.isEmpty()) {
			return Collections.emptyList();
		}
		JSONObject first = array.getJSONObject(0);
		if (first == null) {
			return Collections.emptyList();
		}
		JSONArray detail = first.getJSONArray("detail");
		if (detail == null || detail.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> values = new ArrayList<>();
		for (Object obj : detail) {
			if (obj == null) {
				continue;
			}
			String value = String.valueOf(obj).trim();
			if (!value.isEmpty()) {
				values.add(value);
			}
		}
		return values;
	}

	private String normalizeCsv(String csv) {
		if (csv == null || csv.trim().isEmpty()) {
			return "";
		}
		return Arrays.stream(csv.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.joining(","));
	}

	private List<String> getSpecNamesByRuleIds(List<Long> specAttrIds) {
		if (CollectionUtil.isEmpty(specAttrIds)) {
			return new ArrayList<>();
		}
		List<DiyStoreProductRule> rules = diyStoreProductRuleService.lambdaQuery()
			.in(DiyStoreProductRule::getId, specAttrIds)
			.list();
		if (CollectionUtil.isEmpty(rules)) {
			return new ArrayList<>();
		}
		Map<Long, String> ruleNameMap = rules.stream().collect(Collectors.toMap(
			DiyStoreProductRule::getId,
			DiyStoreProductRule::getRuleName,
			(a, b) -> a
		));
		List<String> names = new ArrayList<>();
		for (Long id : specAttrIds) {
			String name = ruleNameMap.get(id);
			if (!isBlank(name)) {
				names.add(name);
			}
		}
		return names;
	}

	private String normalizeSku(String sku) {
		if (isBlank(sku)) {
			return "";
		}
		return Arrays.stream(sku.split("\\|"))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.joining("|"));
	}

	private boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}
}
