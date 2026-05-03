package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.SystemUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import com.xms.app.entity.bo.DestroyCallbackBo;
import com.xms.app.entity.dto.CardPackageDto;
import com.xms.app.entity.dto.CardUpgradeLogDto;
import com.xms.app.entity.req.*;
import com.xms.app.entity.resp.BuyNodePlanResp;
import com.xms.app.entity.resp.ClaimAirdropResp;
import com.xms.app.entity.resp.CreateActiveOrderResp;
import com.xms.app.entity.vo.AirdropClaimPageInfoVo;
import com.xms.app.entity.vo.AirdropClaimRecordVo;
import com.xms.app.entity.vo.NodePlanVo;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.RedissonDelayHandler;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.constant.*;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.app.service.BizCardService;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.Func;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 卡片类型业务实现类
 * @author xms
 * @date 2023/04/05
 */
@Service
@Slf4j
public class BizCardServiceImpl implements BizCardService {


}
