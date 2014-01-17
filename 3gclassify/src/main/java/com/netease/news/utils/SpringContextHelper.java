package com.netease.news.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * ApplicationContext的帮助类, 用于非spring管理的类获取spring bean.
 *
 * @author dongliu
 */
public class SpringContextHelper implements ApplicationContextAware {

	private static ApplicationContext context;

	/*
	 * 注入ApplicationContext
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		//在加载Spring时自动获得context
		SpringContextHelper.context = context;
	}

	public static Object getBean(String beanName) {
		return context.getBean(beanName);
	}

	public static Object getBean(Class calssz) {
		return context.getBean(calssz);
	}

	public static void publishEvent(ApplicationEvent event) {
		context.publishEvent(event);
	}
}