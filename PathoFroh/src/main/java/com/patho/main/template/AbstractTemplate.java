package com.patho.main.template;

import org.apache.velocity.app.Velocity;

import com.patho.main.model.interfaces.ID;
import com.patho.main.util.VelocityNoOutputLogger;

import lombok.Getter;
import lombok.Setter;

//@Entity
//@Audited
//@SelectBeforeUpdate(true)
//@DynamicUpdate(true)
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@SequenceGenerator(name = "template_sequencegenerator", sequenceName = "template_sequence")
@Getter
@Setter
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
}
