package com.patho.main.util.printer;

import com.patho.main.model.interfaces.ID;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cups4j.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractPrinter implements ID {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Expose
	protected long id;

	@Expose
	protected String address;

	@Expose
	protected String port;

	@Expose
	protected String name;

	@Expose
	protected String description;

	@Expose
	protected String location;

	@Expose
	protected String commentary;

	@Expose
	protected String userName;

	@Expose
	protected String password;

	@Expose
	protected String deviceUri;

	public AbstractPrinter() {
	}

	public abstract boolean printTestPage();
}
