package com.patho.main.action.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.patho.main.config.util.ResourceBundle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class AbstractHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;
	
}
