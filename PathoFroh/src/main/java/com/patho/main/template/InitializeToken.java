package com.patho.main.template;

public class InitializeToken {
	private String key;
	private Object value;

	@java.beans.ConstructorProperties({"key", "value"})
	public InitializeToken(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.value;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}