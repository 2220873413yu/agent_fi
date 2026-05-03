package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import com.xms.app.entity.req.UserAddressReqVo;
import com.xms.app.service.AppUserBizService;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.Func;
import com.xms.common.utils.ObjectUtil;
import com.xms.common.utils.RegexUtil;
import com.xms.common.utils.SecurityUtils;
import com.xms.dao.domain.UserAddress;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.IUserAddressService;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * @author: renengadePISTA
 * @createDate: 2023/9/12
 */
@Service
@AllArgsConstructor
@Slf4j
public class AppUserBizServiceImpl implements AppUserBizService {
	private final static String ANNOTATION_PARAM = "#frontUserId";
	@Autowired
	private UserInfoService userInfoServiceImpl;
	@Autowired
	private IUserAddressService userAddressServiceImpl;

	private ResultPista validatorPhone(String addressPhone) {
		if (!Validator.isMobile(addressPhone) && !RegexUtil.isHKPhoneLegal(addressPhone) &&
			!RegexUtil.isTWPhoneLegal(addressPhone) && !RegexUtil.isMCPhoneLegal(addressPhone)) {
			throw new ServiceException(ResponseCode.CODE_1256);
		}
		return ResultPista.success();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResultPista userAddressAdd(UserAddressReqVo req, Long frontUserId) {
		//校验手机号
		ResultPista resultPista = validatorPhone(req.getPhone());
		if (!resultPista.isSuccess()) {
			return resultPista;
		}
		if(req.getProvince().length()>50 || req.getCity().length()>50 || req.getArea().length()>50
		 || req.getDetailed().length()>100 || req.getUserName().length()>50){
			throw new ServiceException(ResponseCode.CODE_1257);
		}
		//  BY RENEGADE PISTA: 2023/6/12  判断默认地址
		resultPista = handerState(req, frontUserId);
		if (!resultPista.isSuccess()) {
			return resultPista;
		}
		UserAddress userAddress = new UserAddress();
		BeanUtil.copyProperties(req,userAddress);
		userAddress.setUserId(frontUserId);
		userAddressServiceImpl.save(userAddress);
		return ResultPista.success();
	}

	private ResultPista handerState(UserAddressReqVo req, long frontUserId) {
		UserAddress addressDefault = userAddressServiceImpl.lambdaQuery().eq(UserAddress::getUserId, frontUserId).eq(UserAddress::getAddressState, SysConstant.ONE).one();
		if (addressDefault == null) {
			// 为空那么就是默认
			req.setAddressState(SysConstant.ONE);
		} else {
			if (req.getAddressState().equals(SysConstant.ONE)) {
				boolean res = userAddressServiceImpl.lambdaUpdate().set(UserAddress::getAddressState, SysConstant.ZERO)
					.eq(UserAddress::getAddressState, SysConstant.ONE).eq(UserAddress::getUserId, frontUserId).update();
				if (!res) {
					throw new ServiceException(ResponseCode.CODE_1002);
				}
			}
		}
		return ResultPista.success();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResultPista userAddressUpdate(UserAddressReqVo req, Long frontUserId) {
		if (Func.isAllEmpty(req.getId())) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		Long count = userAddressServiceImpl.lambdaQuery()
			.eq(UserAddress::getId, req.getId())
			.eq(UserAddress::getUserId, frontUserId)
			.count();
		if (count <= 0) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		//校验手机号
		ResultPista resultPista = validatorPhone(req.getPhone());
		if (!resultPista.isSuccess()) {
			return resultPista;
		}
		//  BY RENEGADE PISTA: 2023/6/12  判断默认地址
		resultPista = handerState(req, frontUserId);
		if (!resultPista.isSuccess()) {
			return resultPista;
		}
		UserAddress userAddress = new UserAddress();
		BeanUtil.copyProperties(req,userAddress);
		userAddress.setUserId(frontUserId);
		boolean r = userAddressServiceImpl.updateById(userAddress);
		if (!r) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		return ResultPista.success();
	}

	@Override
	public ResultPista userAddressRemove(Long id, Long frontUserId) {
		if (Func.isAllEmpty(id)) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		userAddressServiceImpl.deleteAddressById(id,frontUserId);
		return ResultPista.success();
	}

	@Override
	public ResultPista<List<UserAddress>> userAddressList() {
		List<UserAddress> userAddresses = userAddressServiceImpl.lambdaQuery()
			.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
			.orderByDesc(UserAddress::getAddressState)
			.orderByDesc(UserAddress::getId).list();
		return ResultPista.data(userAddresses);
	}

	@Override
	public ResultPista<UserAddress> defaultUserAddress() {
		UserAddress userAddresses = userAddressServiceImpl.lambdaQuery()
			.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
			.eq(UserAddress::getAddressState, SysConstant.ONE).one();
		return ResultPista.data(userAddresses);
	}

	@Override
	public ResultPista<UserAddress> getUserAddressById(Long id) {
		if (ObjectUtil.isEmpty(id)) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		UserAddress userAddress = userAddressServiceImpl.lambdaQuery()
			.eq(UserAddress::getId, id)
			.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
			.one();
		return ResultPista.data(userAddress);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResultPista updateDefaultAddressById(Long id) {
		if (ObjectUtil.isEmpty(id)) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		userAddressServiceImpl.lambdaUpdate()
			.set(UserAddress::getAddressState, SysConstant.ZERO)
			.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
			.update();
		userAddressServiceImpl.lambdaUpdate()
			.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
			.eq(UserAddress::getId, id)
			.set(UserAddress::getAddressState, SysConstant.ONE)
			.update();
		return ResultPista.success();
	}
}
