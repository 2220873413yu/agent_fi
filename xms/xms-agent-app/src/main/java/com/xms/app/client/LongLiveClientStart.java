///*
// * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & dreamlu.net).
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.xms.app.client;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.map.MapUtil;
//import cn.hutool.crypto.digest.MD5;
//import com.alibaba.fastjson2.JSONObject;
//import com.xms.app.server.WsStockService;
//import com.xms.common.constant.SysConstant;
//import com.xms.common.core.domain.entity.SysDictData;
//import com.xms.common.utils.DictUtils;
//import com.xms.dao.service.IMarketTradeConfigService;
//import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.stream.Collectors;
//
///**
// * 用户等级处理
// *
// * @author MIER
// */
//@Slf4j
//@Component
//@AllArgsConstructor
//public class LongLiveClientStart {
//	@Autowired
//	private WsStockService wsStockServiceImpl;
//	@Autowired
//	private IMarketTradeConfigService marketTradeConfigServiceImpl;
//	@Async("asyncVirtualExecutor")
//	public void handerWsMsg(Map<String, Object> fundCodeSub) throws Exception {
//		//List<SysDictData> sysDictData = DictUtils.getDictCache("exchange_contract_type");
//		JSONObject event = new JSONObject(), data = new JSONObject();
//		StringBuilder wlist = new StringBuilder();
//
//		//if (sysDictData != null && !sysDictData.isEmpty()) {
//			//订阅市场代码，多个以逗号分割。如:SH,SZ
//			//List<SysDictData> collect = new ArrayList<>();
////				sysDictData.stream()
////				.filter(e -> !"NYMEX".equals(e.getDictValue()))
////				.collect(Collectors.toList());
//			//data.put("market", CollUtil.join(collect, ",",SysDictData::getDictValue));
//			data.put("market", "WI,WX,WA");
//
//			//产品白名单过滤【可选参数】，多个以逗号分割。如:SH600(A股过滤),SZ000002(单个产品过滤)。最多可指定50个
//			// data.put("wlist", wlist.toString().trim());
//			//产品黑名单过滤【可选参数】，多个以逗号分割。如:SH600(A股过滤),SZ000002(单个产品过滤)。最多可指定50个
//			if (!wlist.toString().trim().isEmpty()) {
//				data.put("wlist", wlist.toString().trim());
//			}
//			event.put("event", GlobalConstant.SUBSCRIBE);
//			event.put("data", data);
//			GlobalConstant.GLOBAL_GROUP_CHANNEL.add(JSONObject.toJSONString(event));
//		//}
//
//		createWsMsg(fundCodeSub);
//	}
//
//	/**
//	 * 注意点：目前限制了最多20个不同的ws地址客户端没，超过20个，就需要做集群了。
//	 *
//	 * @param fundCodeSub
//	 * @throws Exception
//	 */
//	public void createWsMsg(Map<String, Object> fundCodeSub) throws Exception {
//		String wsUrl = MapUtil.getStr(fundCodeSub, TcpClient.WS_URL);
//		CountDownLatch countDownLatch = new CountDownLatch(SysConstant.ONE);
//		TcpClient webSocketClient = new TcpClient(wsUrl, SysConstant.ONE.toString(), countDownLatch,
//			MapUtil.getStr(fundCodeSub, "mode"),wsStockServiceImpl, marketTradeConfigServiceImpl);
//		webSocketClient.connect();
//		countDownLatch.await();
//		String stamp = String.valueOf(System.currentTimeMillis() / 1000);
//		String str = "u=" + fundCodeSub.get("userName") + "&p=" + fundCodeSub.get("password") + "&stamp=" + stamp;
//		JSONObject json = new JSONObject(), event = new JSONObject();
//		json.put("u", fundCodeSub.get("userName"));
//		json.put("sign", MD5.create().digestHex(str));
//		json.put("stamp", stamp);
//		event.put("event", "login");
//		event.put("data", json);
//		webSocketClient.getChannel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(event)));
//		log.info("兜底的数海登录成功要发送的数据是：{}", event);
//	}
//
//
//}
