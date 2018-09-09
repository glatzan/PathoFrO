package com.patho.main.util.exception;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadExceptionHandler implements AsyncUncaughtExceptionHandler {

	public ThreadExceptionHandler() {
	}

	@Override
	public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
		System.out.println("Exception message - " + throwable.getMessage());
		System.out.println("Method name - " + method.getName());
		for (Object param : obj) {
			System.out.println("Parameter value - " + param);
		}
		log.debug("Exception occurred::" + throwable);
	}

}
