package com.patho.main.template;

import com.patho.main.model.interfaces.ID;
import com.patho.main.util.VelocityNoOutputLogger;
import org.apache.velocity.app.Velocity;

//@Entity
//@Audited
//@SelectBeforeUpdate(true)
//@DynamicUpdate(true)
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@SequenceGenerator(name = "template_sequencegenerator", sequenceName = "template_sequence")
public abstract class AbstractTemplate implements ID, Cloneable {

	protected long id;

	protected String name;

	protected String content;

	protected String content2;

	/**
	 * Type of the template
	 */
	protected String type;

	/**
	 * If True the default of it's type
	 */
	protected boolean defaultOfType;

	/**
	 * If true the generated content should not be saved in the database
	 */
	protected boolean transientContent;

	/**
	 * Name of the template class
	 */
	protected String templateName;

	/**
	 * Addition atributes, e.g. for printing
	 */
	protected String attributes;

	public void prepareTemplate() {

	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public static final void initVelocity() {
		Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityNoOutputLogger());
		Velocity.init();
	}
	

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof AbstractTemplate && ((AbstractTemplate) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getContent() {
		return this.content;
	}

	public String getContent2() {
		return this.content2;
	}

	public String getType() {
		return this.type;
	}

	public boolean isDefaultOfType() {
		return this.defaultOfType;
	}

	public boolean isTransientContent() {
		return this.transientContent;
	}

	public String getTemplateName() {
		return this.templateName;
	}

	public String getAttributes() {
		return this.attributes;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setContent2(String content2) {
		this.content2 = content2;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDefaultOfType(boolean defaultOfType) {
		this.defaultOfType = defaultOfType;
	}

	public void setTransientContent(boolean transientContent) {
		this.transientContent = transientContent;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
}
