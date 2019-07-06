package com.patho.main.util.printer;

import com.google.gson.annotations.Expose;
import com.patho.main.model.interfaces.ID;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
