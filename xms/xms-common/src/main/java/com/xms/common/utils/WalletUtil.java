package com.xms.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.binance.connector.client.common.ApiException;
import com.binance.connector.client.common.ApiResponse;
import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.wallet.rest.api.WalletRestApi;
import com.binance.connector.client.wallet.rest.model.QueryUserWalletBalanceResponse;
import com.binance.connector.client.wallet.rest.model.QueryUserWalletBalanceResponseInner;
import com.binance.connector.client.wallet.rest.model.WithdrawRequest;
import com.binance.connector.client.wallet.rest.model.WithdrawResponse;
import com.uduncloud.sdk.client.UdunClient;
import com.uduncloud.sdk.domain.Address;
import com.uduncloud.sdk.domain.ResultMsg;
import com.xms.common.config.UdunConfig;
import com.xms.common.constant.ConstantType.user_money_coin_type;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class WalletUtil {

	@Autowired
	private UdunConfig udunConfig;

	public static void main(String[] args) {
//		UdunClient udunClient = new UdunClient("https://sig11.udun.io",
//			"321160", "94286da2d51a6548746d7015f4964772", "https://www.gylk.xyz/dev-web/udun/callback");


//		UdunClient udunClient = new UdunClient("https://sig10.udun.io",
//			"320819", "40c6a16af4faedb1c7abaae5979630ee", "https://www.gylk.xyz/dev-web/udun/callback");


		String address="0x67EeEf4DAA05a635d09D43200e34559b8C89775f";
		//320994
		//66b6e53c8c982ff7de28e59771332f85
		BigDecimal amount=new BigDecimal("4.9");
		String mainBepCoinType="2510";
		String USDTBEP20="0x55d398326f99059ff775485246999027b3197955";
		//订单号
		String businessId="1907256819540090880";
		String memo="";
		String callUrl="https://www.gylk.xyz/dev-web/udun/callback";
//		System.out.println(udunClient.createAddress("195"));
//		System.out.println(udunClient.checkAddress("2510","0xe254Faa32676F35Ffaaa7BadB06e35DDEAE9374A"));
//		ResultMsg withdraw = udunClient.withdraw(address, amount, mainBepCoinType, USDTBEP20,businessId, memo, callUrl);
//		System.out.println(withdraw);
		//Address addressBSC = udunClient.createAddress("2510");
		//System.out.println(addressBSC.getAddress());
		//udunClient
	}

	/**
	 * 2510	USDT-BEP-20
	 * 195	USDT-TRC-20
	 * 创建钱包地址
	 *
	 * @return
	 */
	public String createAddress(Integer coinType) {
		Address addressBSC = null;
		if (coinType == 2510) {
			addressBSC = getClient().createAddress(udunConfig.getMainBepCoinType());
		}
		return addressBSC.getAddress();
	}

	/**
	 * 校验地址
	 *
	 * @param coinType 2510	USDT-BEP-20
	 *                 * 195	USDT-TRC-20
	 * @param address
	 * @return
	 */
	public boolean checkAddress(Integer coinType, String address) {
		if (coinType == 2510) {
			return getClient().checkAddress(udunConfig.getMainBepCoinType(), address);
		}
		return false;
	}

	/**
	 * 提现
	 *
	 * @param address    提现地址
	 * @param amount     金额
	 * @param coinType   币种
	 * @param businessId 业务号
	 * @param memo       备注
	 * @return
	 */
	public ResultMsg withdraw(String address, BigDecimal amount, Integer coinType, String businessId, String memo) {
		//bep20
		if (coinType == 2510) {
			return getClient().withdraw(address, amount, udunConfig.getMainBepCoinType(), udunConfig.getUSDTBEP20(), businessId, memo, udunConfig.getCallUrl());
		}
		return null;
	}

	private UdunClient getClient() {
		//创建地址
		UdunClient udunClient = new UdunClient(udunConfig.getDomain(),
			udunConfig.getMerchant(), udunConfig.getKey(), udunConfig.getCallUrl());
		return udunClient;
	}

	public int getCoin(String coinType) {
		if (coinType.equalsIgnoreCase(udunConfig.getUSDTBEP20())) {
			return user_money_coin_type.type_1;
		}
		return 0;
	}

	/**
	 * 获取bsc钱包里面资金账户的余额
	 * @param bizType 0:app,1:管理后台
	 * @return
	 */
	public BigDecimal getWalletFundingBalance(Integer bizType){
			try {
				SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
				signatureConfiguration.setApiKey(udunConfig.getBscApiKey());
				signatureConfiguration.setSecretKey(udunConfig.getBscSecretKey());
				ClientConfiguration clientConfiguration = new ClientConfiguration();
				clientConfiguration.setSignatureConfiguration(signatureConfiguration);
				WalletRestApi api = new WalletRestApi(clientConfiguration);
				QueryUserWalletBalanceResponse usdt = api.queryUserWalletBalance("USDT", 5000L).getData();
				for (
					QueryUserWalletBalanceResponseInner queryUserWalletBalanceResponseInner : usdt) {
					if(queryUserWalletBalanceResponseInner.getWalletName().equalsIgnoreCase("Funding")){
						return new BigDecimal(queryUserWalletBalanceResponseInner.getBalance());
					}
				}
			}catch (Exception e){
				if(bizType == 0){
					return BigDecimal.ZERO;
				}else{
					JSONObject jsonObject = JSONObject.parseObject(((ApiException) e).getResponseBody());
					if(jsonObject == null){
						throw new ServiceException("获取币安钱包账户余额超时");
					}else{
						String msg = jsonObject.getString("msg");
						throw new ServiceException(msg);
					}

				}

			}
			return BigDecimal.ZERO;
		}

	/**
	 * 币安提现 bsc链 使用的是资金账户
	 * @param address 提现地址
	 * @param amount 提现金额 写1就是1u 最低10u起提现
	 * @param orderId 订单号
	 * @param bizType 0:app,1:管理后台
	 * @return
	 */
	public ResultMsg bscWithdraw(String address, BigDecimal amount,String orderId,Integer bizType) {
		SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
		signatureConfiguration.setApiKey(udunConfig.getBscApiKey());
		signatureConfiguration.setSecretKey(udunConfig.getBscSecretKey());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setSignatureConfiguration(signatureConfiguration);
		WalletRestApi api = new WalletRestApi(clientConfiguration);
		WithdrawRequest withdrawRequest = new WithdrawRequest();
		//币种 usdt
		withdrawRequest.coin("USDT");
		//bsc 链
		withdrawRequest.network("BSC");
		//提现单号
		withdrawRequest.withdrawOrderId(orderId);
		withdrawRequest.address(address);
		//最低10u起提
		withdrawRequest.amount(amount.doubleValue());
		//資金賬戶
		withdrawRequest.walletType(1L);
		try {
			ApiResponse<WithdrawResponse> response = api.withdraw(withdrawRequest);
			System.out.println(response.getData());
		}catch (Exception e){
			if(bizType == 0){
				//throw new ServiceException(ResponseCode.CODE_1220);
			}else{
				JSONObject jsonObject = JSONObject.parseObject(((ApiException) e).getResponseBody());
				String msg = jsonObject.getString("msg");
				throw new ServiceException(msg);
			}
		}
		return null;
	}


}

