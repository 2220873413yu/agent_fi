package com.xms.app.service;

import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.*;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.entity.vo.PledgeStakeHostingAfiVo;
import com.xms.app.entity.vo.StopStakeHostingOrderVo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.RewardRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管业务Service
 */
public interface BizStakeHostingService {
	/**
	 * 查询 App 托管套餐列表。
	 *
	 * 只返回前端展示和下单需要的套餐字段，不直接暴露数据库对象。
	 *
	 * @return 已上架托管套餐展示列表
	 */
	List<StakeHostingPackageDto> packageList();

	/**
	 * 创建用户侧托管待支付订单。
	 *
	 * <p>该方法只创建待链上支付的托管订单，不扣减站内钱包余额；支付成功由外部回调推进订单生效。</p>
	 *
	 * @param req 创建托管订单请求，金额单位为 USDT，包含钱包签名随机数
	 * @param userId 当前登录用户ID
	 * @return 待支付订单号和订单快照金额
	 */
	ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId);

	/**
	 * 查询我的托管订单列表。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @param status 业务状态，空表示全部状态
	 * @return 当前登录用户托管订单展示列表
	 */
	List<StakeHostingOrderDto> orderList(Long lastId, Integer status);

	/**
	 * 停止当前用户的1天自动复投托管订单。
	 *
	 * <p>该入口只做App层结算时间窗口校验和当前用户透传，实际退本、回退业绩和等级重算由DAO订单服务在事务内完成。</p>
	 *
	 * @param req 停止托管请求，包含订单ID
	 * @return success表示停止成功
	 */
	ResultPista<String> stop(StopStakeHostingOrderVo req);

	/**
	 * 查询可提交 AFI 质押加速的托管订单列表。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @return 当前登录用户可加速订单展示列表
	 */
	List<StakeHostingOrderDto> accelerateOrderList(Long lastId);

	/**
	 * 查询 AFI 质押加速配置套餐。
	 *
	 * @return 已启用 AFI 加速配置展示列表
	 */
	List<StakeHostingAfiAccelerateConfigDto> afiAccelerateConfigList();

	/**
	 * 查询托管订单详情。
	 *
	 * @param id 托管订单ID
	 * @return 当前登录用户托管订单详情展示对象
	 */
	StakeHostingOrderDto orderDetail(Long id);

	/**
	 * 提交 AFI 质押加速。
	 *
	 * <p>按当前登录用户、托管订单和加速配置创建 AFI 质押记录，具体资产处理由 DAO 质押服务完成。</p>
	 *
	 * @param req AFI 质押加速请求，包含托管订单ID、配置ID和钱包签名信息
	 * @return AFI 质押加速记录展示对象
	 */
	ResultPista<StakeHostingAfiPledgeDto> pledgeAfi(PledgeStakeHostingAfiVo req);

	/**
	 * 托管订单链上支付回调。
	 *
	 * <p>验签通过后按订单号、交易 hash 和实付 USDT 金额确认支付，订单幂等和状态推进由 DAO 订单服务保证。</p>
	 *
	 * @param req 外部支付回调参数，包含订单号、链上 hash、实付金额和签名
	 * @return success 表示回调处理完成或幂等成功
	 */
	ResultPista<String> orderCallback(StakeOrderBo req);

	/**
	 * 订单静态收益数据
	 * @param orderNo 托管订单号
	 * @param lastId
	 * @return
	 */
	List<OrderRewardDto> orderRewardList(String orderNo, Long lastId);


	/**
	 * 获取质押信息
	 * @return
	 */
	BigDecimal stakeHostingInfo();
}
