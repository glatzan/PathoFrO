package com.patho.main.test;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
public class TestHandler {

	public String test() {
		return "test";
	}
}
