//
//package com.xms.app.client.init;
//
//import com.xms.app.client.LongLiveClientStart;
//import com.xms.app.client.TcpClient;
//import com.xms.common.config.redis.XmsRedis;
//import com.xms.common.constant.Constants;
//import com.xms.common.utils.Func;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 启动ws客户端监听
// *
// * @author MIER
// */
//@Slf4j
//@Component
//@AllArgsConstructor
//public class ClientStart implements ApplicationRunner {
//	private final LongLiveClientStart wsClientStart;
//	private final Environment environment;
//
//	/**
//	 * Callback used to run the bean.
//	 *
//	 * @param args incoming application arguments
//	 * @throws Exception on error
//	 */
//	@Override
//	public void run(ApplicationArguments args) throws Exception {
//		String profile = environment.getProperty(Constants.ACTIVE_PROFILES_PROPERTY);
//		log.info("初始化数据开始");
//		//初始化港交所行情数据
//		List<Map<String, Object>> wsUrs = new ArrayList<>();
//		boolean clientFlag = true;
//		if (clientFlag) {
//			Map<String, Object> map = new HashMap<>(4);
//			map.put(TcpClient.WS_URL, "ws://real.cnshuhai.com:17381/stock");
//			map.put("mode", profile);
//			map.put("userName", "SIGMA");
//			map.put("password", "SIGMA@13");
//			wsUrs.add(map);
//		}
//		//todo 后续做主备的话。通过redis的分布式锁抢占式的链接
//		for (Map<String, Object> fundCodeSub : wsUrs) {
//			wsClientStart.handerWsMsg(fundCodeSub);
//		}
//	}
//
//
//}
