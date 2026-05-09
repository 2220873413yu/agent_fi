package com.xms.dao.service.impl;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.*;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.SignUtil;
import com.xms.common.utils.WalletUtil;
import com.xms.dao.api.PullThirdInfo;
import com.xms.dao.entity.bo.WithdrawalBo;
import com.xms.dao.entity.domain.Withdrawal;
import com.xms.dao.mapper.WithdrawalMapper;
import com.xms.dao.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 提现表 服务实现类
 * </p>
 *
 * @since 2023-06-30
 */
@Service
@Slf4j
public class WithdrawalServiceImpl extends ServiceImpl<WithdrawalMapper, Withdrawal> implements WithdrawalService {

	@Autowired
	private UserWalletService userWalletServiceImpl;

	@Autowired
	private ISysParaService sysParaServiceImpl;

	@Value("${lq.md5Key}")
	private String md5Key;

	@Value("${lq.baseUrl}")
	private String baseUrl;

	/**
	 * 查询提现列表
	 *
	 * @param withdrawal 提现
	 * @return 提现
	 */
	@Override
	public List<Withdrawal> selectWithdrawalList(Withdrawal withdrawal) {
		List<Withdrawal> withdrawals = baseMapper.selectWithdrawalList(withdrawal);
		return withdrawals;
	}

	@Override
	@RedisLock(value = RedisConstant.LockConstant.XMS_WITHDRAW_CHECK, param = "#withdrawal.id")
	@Transactional(rollbackFor = Exception.class)
	public AjaxResult updateCheckStatusById(Withdrawal withdrawal) {
		if (!withdrawal.getStatus().equals(SysConstant.ONE) && !withdrawal.getStatus().equals(SysConstant.TWO)) {
			return AjaxResult.error("请选择审核状态");
		}
		Withdrawal temp = getById(withdrawal.getId());
		if (!temp.getStatus().equals(SysConstant.ZERO)) {
			return AjaxResult.error("提现记录已被审核,刷新页面后重试");
		}
		if(withdrawal.getStatus() == 1){
			//审核成功的话状态直接修改为提现成功
			withdrawal.setStatus(3);
		}
		boolean res = lambdaUpdate()
			.eq(Withdrawal::getId, withdrawal.getId())
			.eq(Withdrawal::getStatus, SysConstant.ZERO)
			.set(Withdrawal::getStatus, withdrawal.getStatus())
			.set(Withdrawal::getUpdateTime, new Date())
			.update();
		if (!res) {
			return AjaxResult.error();
		}

		if (withdrawal.getStatus().equals(SysConstant.TWO)) {
			//1.退还提现金额
			int i = userWalletServiceImpl.handerUserMoney(temp.getChangeBalance(), temp.getCode(), temp.getUserId(), temp.getUserId()
				, ConstantType.user_money_log_source_type.type_5, temp.getCoinType());
			if (i != 1) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return AjaxResult.error();
			}
		}else if(withdrawal.getStatus().equals(SysConstant.THREE)){
			//发送到合约(内扣)
			String tokenName= "USDT";
			Map<String, Object> formParams = new HashMap<>();
			formParams.put("orderId", temp.getCode());
			formParams.put("userAddress", temp.getAccountNo());
			formParams.put("token", tokenName);
			BigDecimal finalAmount = temp.getChangeBalance().subtract(temp.getFeeBalance());
			formParams.put("amount", finalAmount.stripTrailingZeros().toPlainString());
			String sign = SignUtil.getSign(formParams, false, false, md5Key);
			formParams.put("sign", sign);
			log.info("提现业务完整参数 param:{},sign:{}", formParams,sign);
			sendWithdrawalRequest(formParams);
		}
		return AjaxResult.success();
	}

	/**
	 * 发起提现请求
	 *
	 * @param formParams
	 */
	public void sendWithdrawalRequest(Map<String, Object> formParams) {
		// 使用HttpRequest创建请求对象
		String url = baseUrl + "/chain/withdraw";
		HttpRequest request = HttpUtil.createPost(url)
			.body(JSON.toJSONString(formParams)) // JSON 形式传参
			.header("Content-Type", "application/json")
			.header("Custom-Header", "HeaderValue")
			.timeout(5000); // 设置超时时间（毫秒）
		// 发送请求并获取响应
		HttpResponse response;
		try {
			response = request.execute();
		} catch (IORuntimeException ex) {
			log.error("withdrawal request timeout, params:{}, url:{}", formParams, url, ex);
			throw new ServiceException("提现通道请求超时，请稍后重试");
		}

		// 获取响应状态码
		int statusCode = response.getStatus();
		log.info("Status Code:{}", statusCode);

		// 获取响应体
		String responseBody = response.body();
		log.info("responseBody:{}", responseBody);
		JSONObject jsonResponse = JSONUtil.parseObj(response.body());
		Integer code = jsonResponse.getInt("code");
		if (code == null || !code.equals(200)) {
			String errorMsg = jsonResponse.getStr("error");
			if(StrUtil.isNotBlank(errorMsg)){
				throw new ServiceException(errorMsg);
			}
			throw new ServiceException(jsonResponse.getStr("message"));
		}
	}

	@Override
	public BigDecimal selectUsdtTotalBalance() {
		return baseMapper.selectUsdtTotalBalance();
	}

	@Override
	public BigDecimal selectBPayTotalBalance() {
		return baseMapper.selectBPayTotalBalance();
	}

	/**
	 * 提现记录
	 * @param pageIndex 当前页 默认1
	 * @param pageSize 每页长度 默认20(最大20)
	 * @return
	 */
	@Override
	public PageInfo<WithdrawalBo> listWithdrawRecord(Integer coinType, Integer pageIndex, Integer pageSize,Long userId) {
		PageHelper.startPage(pageIndex, pageSize);

		PageInfo<WithdrawalBo> pageInfo = new PageInfo<WithdrawalBo>(this.baseMapper.listWithdrawRecord(coinType,userId));
		return pageInfo;
	}

}
