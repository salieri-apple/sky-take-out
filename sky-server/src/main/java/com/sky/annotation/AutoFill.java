package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解，用于标识某个方法需要进行功能字段自动填充处理
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
	//数据库操作类型为：UPDATE INSERT
	/*value 是 Java 自定义注解中的一个特殊属性名，它被称为 “默认属性”（Default Member）。
	它的特殊之处在于：当注解只有一个属性，且该属性名为 value 时，使用注解时可以省略 value= 这部分，直接写值。*/
	OperationType value();
}
