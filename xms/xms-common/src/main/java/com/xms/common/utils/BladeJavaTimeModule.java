package com.xms.common.utils;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * java 8 时间默认序列化
 *
 *
 */
public class BladeJavaTimeModule extends SimpleModule {

	public BladeJavaTimeModule() {
		super(PackageVersion.VERSION);
		this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(CopyProperty.DateTimeUtil.DATETIME_FORMAT));
		this.addDeserializer(LocalDate.class, new LocalDateDeserializer(CopyProperty.DateTimeUtil.DATE_FORMAT));
		this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(CopyProperty.DateTimeUtil.TIME_FORMAT));
		this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(CopyProperty.DateTimeUtil.DATETIME_FORMAT));
		this.addSerializer(LocalDate.class, new LocalDateSerializer(CopyProperty.DateTimeUtil.DATE_FORMAT));
		this.addSerializer(LocalTime.class, new LocalTimeSerializer(CopyProperty.DateTimeUtil.TIME_FORMAT));
	}

}
