package com.xms.app.service;

import com.xms.app.entity.BeanDto;
import com.xms.app.entity.BeanPo;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体类转换接口，比beanutil的反射更高的性能
 *
 * @author: renengadePISTA
 * @createDate: 2024/5/30
 * 标识一个接口为MapStruct映射接口。通过componentModel属性，可以指定生成的映射实现类的组件模型，如Spring的componentModel = "spring"。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TestBeanUtils {
	@Named("intToString")
	static Integer intToString(String num) {
		return Integer.parseInt(num);
	}

	@Named("intToStringOnList")
	static List<Integer> intToString(List<String> nums) {
		return nums.stream().map(Integer::parseInt).collect(Collectors.toList());
	}

	BeanDto poToDto(BeanPo po);

	/**
	 * @param po
	 * @return
	 * @Mapping 注解用于配置字段之间的映射关系。可以指定源属性、目标属性、以及转换表达式等。
	 */
	@Mapping(source = "time", target = "newTime")
	BeanDto personToPersonDTO(BeanPo po);

	/**
	 * 自定义映射逻辑
	 *
	 * @param po
	 * @return
	 */
	@Mapping(target = "newTime", expression = "java(mapStatus(po.getTime()))")
	BeanDto personToDiyPersonDTO(BeanPo po);

	default String mapStatus(String status) {
		// 自定义映射逻辑
		return "2024-05-29";
	}

	/**
	 * 格式转换
	 *
	 * @param po
	 * @return
	 */
	@Mapping(source = "time", target = "newTime", dateFormat = "yyyy-MM-dd")
	BeanDto personToDiyPersonDTO1(BeanPo po);

	/**
	 * Mappings 用于多个映射组合处理
	 *
	 * @param dto
	 * @return
	 */
	@Mappings({
		@Mapping(target = "time", source = "newTime"),//不同的字段名称可以通过这种方式来进行自动映射
		@Mapping(target = "set", source = "set1"),//不同的字段名称可以通过这种方式来进行自动映射
		@Mapping(target = "age", qualifiedByName = "intToString"),//利用自定义方法来对不同类型的字段进行转换映射
		@Mapping(target = "list", source = "list1", qualifiedByName = "intToStringOnList")//利用自定义方法来对不同类型的字段集合进行转换映射
	})
	BeanPo dtoToPo(BeanDto dto);
}
