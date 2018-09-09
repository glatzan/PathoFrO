package com.patho.main.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class TestComponent {

	@Autowired
	public TestHandler testHandler;

	public String test() {
		return testHandler.test();
	}
}
