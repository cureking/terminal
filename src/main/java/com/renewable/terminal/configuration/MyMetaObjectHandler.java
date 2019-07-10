package com.renewable.terminal.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * 拦截填充公共字段
 */
@Configuration
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
	@Override
	public void insertFill(MetaObject metaObject) {
		// 要配合    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)等注解使用，才有效果

		log.info("start insert fill ....");
		this.setFieldValByName("createBy", "admin", metaObject);
		this.setFieldValByName("createTime", new Date(), metaObject);
//        log.info(metaObject.getValue("createTime").toString());

	}

	@Override
	public void updateFill(MetaObject metaObject) {
		log.info("start update fill ....");
		this.setFieldValByName("updateBy", "admin", metaObject);
		this.setFieldValByName("updateTime", new Date(), metaObject);
	}
}
