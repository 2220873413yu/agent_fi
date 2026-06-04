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
	 * 发送邮箱验证码
	 * @param req
	 * @return
	 */
    ResultPista sendMesAuthCode(MesAuthCodeVo req)  throws Exception;

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
	 * 获取我的直推列表
	 * @return
	 */
	PageInfo<MyDirectMemberDto> listSubMembers(Integer pageIndex, Integer pageSize,Integer gameLevel);

}
