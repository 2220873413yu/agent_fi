package com.xms.dao.service.impl;

import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.EmailConfigMapper;
import com.xms.dao.domain.EmailConfig;
import com.xms.dao.service.IEmailConfigService;

/**
 * 谷歌邮箱配置Service业务层处理
 *
 * @author xms
 * @date 2025-09-18
 */
@Service
public class EmailConfigServiceImpl extends XmsDataServiceImpl<EmailConfigMapper, EmailConfig> implements IEmailConfigService
{


    /**
     * 查询谷歌邮箱配置列表
     *
     *
     * @param emailConfig 谷歌邮箱配置
     * @return 谷歌邮箱配置
     */
    @Override
    public List<EmailConfig> selectEmailConfigList(EmailConfig emailConfig)
    {
        return baseMapper.selectEmailConfigList(emailConfig);
    }

	@Override
	public int deleteRecordById(List<Long> list) {
		if(CollectionUtil.isEmpty(list)){
			return 1;
		}
		return baseMapper.deleteRecordById(list);
	}
}
