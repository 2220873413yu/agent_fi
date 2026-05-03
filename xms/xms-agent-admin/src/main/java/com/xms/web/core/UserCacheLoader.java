//package com.xms.web.core;
//
//import com.xms.dao.entity.domain.UserInfo;
//import com.xms.dao.service.XmsUserInfoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class UserCacheLoader implements ApplicationRunner {
//
//	public static Map<Long, String> userPhoneMap =  new ConcurrentHashMap<>(1500);
//	public static Map<Long, String> userEmailMap =  new ConcurrentHashMap<>(1500);
//
//	@Autowired
//	private XmsUserInfoService userInfoService;
//
//	@Override
//	public void run(ApplicationArguments args) throws Exception {
//		List<UserInfo> userInfoList = userInfoService.lambdaQuery()
//			.select(UserInfo::getUserId, UserInfo::getRegAddress)
//			.list();
//		if(!CollectionUtils.isEmpty(userInfoList)){
//			for (UserInfo userInfo : userInfoList) {
//				//手机号
//				userPhoneMap.put(userInfo.getUserId(), userInfo.getRegAddress());
//				//邮箱
//				userEmailMap.put(userInfo.getUserId(), userInfo.getEmail());
//			}
//		}
//	}
//
//	public String fetchAddressFromDatabase(Long userId) {
//		UserInfo userInfo = userInfoService.lambdaQuery()
//			.eq(UserInfo::getUserId, userId)
//			.select(UserInfo::getRegAddress)
//			.one();
//		return userInfo != null ? userInfo.getRegAddress() : "Unknown address";
//	}
//
//}
