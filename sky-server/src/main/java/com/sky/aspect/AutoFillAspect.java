package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，实现公共字段自动填充处理逻辑
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
	//定义了一个名为 autoFillPointCut() 的切入点（Pointcut）。
	//这个切入点描述了“哪些方法需要被拦截”(com.sky.mapper包下的所有类的所有方法 且 标注了@AutoFill注解的方法)。
	@Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
	public  void autoFillPointCut(){}

	//前置通知，当目标方法（如 insert）被执行之前，会先执行这个autoFill方法
	@Before("autoFillPointCut()")//指定这个通知要绑定到对应的切入点autoFillPointCut上
	public void autoFill(JoinPoint joinPoint){//JoinPoint：可以被AOP控制的方法，可以获得被AOP控制的方法执行时的相关信息，如目标类名、方法名、方法参数等
		log.info("开始进行公共字段自动填充...");

		//获取到当前被拦截的方法上的数据库操作类型
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象（方法签名：方法名+返回类型+声明类+参数类型）
		AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
		OperationType operationType = autoFill.value();//获得数据库操作类型（UPDATE、INSERT）

		//获取到当前被拦截的方法的接收参数--实体对象
		//实体对象不使用Employee，而是使用Object接收是因为，后面还有菜品等模块的实体对象，所以使用Object接收
		Object[] args = joinPoint.getArgs();//args是一个数组，数组中保存的是目标方法执行时传递的实际参数，我们默认将Employee实体放到第一位
		if(args == null || args.length == 0){
			return;
		}

		Object entity = args[0];

		//准备赋值的数据
		LocalDateTime now = LocalDateTime.now();
		Long currentId = BaseContext.getCurrentId();

		//根据当前不同的操作类型，为对应的属性通过反射来赋值
		//为什么不使用Employee的setter，而是使用反射来赋值？因为insert、update的接收参数不仅仅只有Employee，还有菜品分类等模块的实体对象，使用反射是一种”通解“
		if (operationType == OperationType.INSERT){
			//为4个公共字段赋值
			try {
				Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
				Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
				Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
				Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

				//通过反射为对象属性赋值
				setCreateTime.invoke(entity, now);
				setCreateUser.invoke(entity, currentId);
				setUpdateTime.invoke(entity, now);
				setUpdateUser.invoke(entity, currentId);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}else if(operationType == OperationType.UPDATE){
			//为2个公共字段赋值
			try {
				Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
				Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

				//通过反射为对象属性赋值
				setUpdateTime.invoke(entity, now);
				setUpdateUser.invoke(entity, currentId);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
 	}
}
