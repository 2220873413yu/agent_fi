package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.EmailConfig;

/**
 * 谷歌邮箱配置Mapper接口
 *
 * @author xms
 * @date 2025-09-18
 */
public interface EmailConfigMapper extends XmsMapper<EmailConfig>
{
    /**
     * 查询谷歌邮箱配置列表
     *
     * @param emailConfig 谷歌邮箱配置
     * @return 谷歌邮箱配置集合
     */
    public List<EmailConfig> selectEmailConfigList(EmailConfig emailConfig);

	/**
	 * 批量删除谷歌邮箱配置
	 *
	 * @param list 谷歌邮箱配置id集合
	 * @return
	 */
	int deleteRecordById(List<Long> list);
}
