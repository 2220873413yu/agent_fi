package com.xms.dao.mapper;

import com.xms.dao.domain.UserGradeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author MIER
 */
@Mapper
public interface AsyncTaskMapper {

	/**
	 * 查询任务
	 *
	 * @param map
	 * @return
	 */
	Map<String, Object> getTask(@Param("map") Map<String, Object> map);

	/**
	 * 删除任务
	 *
	 * @param map
	 * @return
	 */
	int delTask(@Param("map") Map<String, Object> map);




	/**
	 * 新增任务
	 *
	 * @param map
	 * @return
	 */
	int addTask(@Param("map") Map<String, Object> map);


	/**
	 * 新增升级失败记录
	 *
	 * @param map
	 * @return
	 */
	int addLevelErrorEvent(@Param("map") Map<String, Object> map);


	List<UserGradeDO> getUserSummary(String inviteUserCode);
	List<UserGradeDO> getDirectUserLevel(String inviteUserCode);
	BigDecimal  getUserMaxPerformance(String inviteUserCode);

	@Select("select para_value from t_sys_para where para_code = #{code} limit 1")
	String getParaValue(@Param("code") String code);

    int countByInviteCode(String userCode);
}
