package com.xms.common.config.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 元数据处理
 *
 * @author MIER
 * @date 2020-10-10
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		if (metaObject.getValue("createTime") == null) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			this.setFieldValByName("createTime", time, metaObject);
		}
		// 若deleted值不存在时进行填充
        /*if (metaObject.getValue("deleted") == null) {
            this.setFieldValByName("deleted", 0, metaObject);
        }*/
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		Object o = metaObject.getValue("updateTime");
		if (o == null ||  ((Date) o).getTime() < System.currentTimeMillis()) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			this.setFieldValByName("updateTime", time, metaObject);
		}
	}
}
