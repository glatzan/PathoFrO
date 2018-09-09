package com.patho.main.config.util;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {

		System.out.println("Exception Cause - " + throwable.getMessage());
		System.out.println("Method name - " + method.getName());
		for (Object param : obj) {
			System.out.println("Parameter value - " + param);
		}
	}
}