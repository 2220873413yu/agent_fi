package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.EmailConfig;

/**
 * 谷歌邮箱配置Service接口
 *
 * @author xms
 * @date 2025-09-18
 */
public interface IEmailConfigService extends XmsDataService<EmailConfig>
{

    /**
     * 查询谷歌邮箱配置列表
     *
     * @param emailConfig 谷歌邮箱配置
     * @return 谷歌邮箱配置集合
     */
    public List<EmailConfig> selectEmailConfigList(EmailConfig emailConfig);

	/**
	 * 删除记录
	 * @param list
	 * @return
	 */
	int deleteRecordById(List<Long> list);
}
