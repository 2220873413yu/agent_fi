package com.xms.app.service;

import com.github.pagehelper.PageInfo;
import com.xms.app.entity.LoginBo;
import com.xms.app.entity.TeamOverviewDto;
import com.xms.app.entity.bo.CoinInfoBo;
import com.xms.app.entity.bo.ComputingPowerBo;
import com.xms.app.entity.bo.TeamViewBO;
import com.xms.app.entity.bo.UserAssetInfoBo;
import com.xms.app.entity.dto.CurrentStakeHostingStaticRateDto;
import com.xms.app.entity.dto.MyDirectMemberDto;
import com.xms.app.entity.dto.MyTeamInfoDto;
import com.xms.app.entity.dto.MyTeamMemberDto;
import com.xms.app.entity.dto.MyTeamMemberPageDto;
import com.xms.app.entity.req.BindEmailVo;
import com.xms.app.entity.req.BindGoogleCodeVo;
import com.xms.app.entity.req.BindInviteUserReq;
import com.xms.app.entity.req.UserBaseInfoVo;
import com.xms.app.entity.vo.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.core.domain.model.xms.LoginAppUser;
import com.xms.dao.entity.bo.BatchUserBo;
import com.xms.dao.entity.bo.UserMoneyBo;
import com.xms.dao.entity.domain.UserMoneyLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @author: renengadePISTA
 * @createDate: 2023/9/12
 */
public interface BizUserService {
	/**
	 * 注册接口
	 * @param req
	 * @return
	 * @throws Exception
	 */
	ResultPista register(RegisterSmsVo req) throws Exception;

	/**
	 * 退出登录
	 * @param request
	 */
	ResultPista logout(HttpServletRequest request);



	/**
	 * 修改用户基础信息
	 * @param req
	 */
	void updateBaseInfo(@Valid UserBaseInfoVo req);



	/**
	 * 发送邮箱验证码
	 * @param req
	 * @return
	 */
    ResultPista sendMesAuthCode(MesAuthCodeVo req)  throws Exception;

	/**
	 * 绑定邮箱
	 * @param req
	 */
	void bindEmail(BindEmailVo req);

	/**
	 * 获取随机数
	 * @param address
	 * @return
	 */
	String getMessage(String address);

	/**
	 * 登录接口
	 * @param loginVo
	 * @return
	 */
	ResultPista<LoginAppUser> login(LoginVo loginVo);

	ResultPista<LoginAppUser> login(BatchUserBo req);


	/**
	 * 获取我的团队数据
	 * @param userId
	 * @return
	 */
	MyTeamInfoDto myTeamInfo(Long userId);

	/**
	 * 查询当前登录用户今日G值和托管基础静态日利率。
	 *
	 * @param userId 用户ID
	 * @return 今日G值和当前托管基础静态日利率
	 */
	CurrentStakeHostingStaticRateDto currentStakeHostingStaticRate(Long userId);

	/**
	 * 我的团队数据 总成员、直推人数、团队销毁usdt、等级
	 * @param lastId lastId
	 * @param distance 层级
	 * @return
	 */
	MyTeamMemberPageDto listMyTeamMembers(Long lastId, Integer distance, Integer level);

	/**
	 * 我的团队数据
	 * @return
	 */
	List<MyDirectMemberDto> listMyDirectMembers();

	/**
	 * 绑定邀请用户
	 * @param req
	 * @return
	 */
	ResultPista bindInviteUser(BindInviteUserReq req);

	/**
	 * 获取用户业绩数据
	 * @return
	 */
    TeamViewBO getTeamView(Long userId);

	/**
	 * 获取算力页面展示数据
	 * @return
	 */
	ComputingPowerBo computingPowerData();

	/**
	 * 获取算力奖励产出列表
	 * @return
	 */
	List<UserMoneyLog> powerDataList(Long lastId);

	/**
	 * 获取我的直推列表
	 * @return
	 */
	PageInfo<MyDirectMemberDto> listSubMembers(Integer pageIndex, Integer pageSize,Integer gameLevel);

	ResultPista<LoginAppUser> getToken(String address);
}
