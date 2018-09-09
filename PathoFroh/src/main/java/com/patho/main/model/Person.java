package com.patho.main.model;

import static org.hibernate.annotations.LazyCollectionOption.FALSE;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patho.main.common.Dialog;
import com.patho.main.model.interfaces.ArchivAble;
import com.patho.main.model.interfaces.FullName;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.LogAble;

import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "person_sequencegenerator", sequenceName = "person_sequence")
@Getter
@Setter
public class Person implements Serializable, LogAble, ArchivAble, ID, FullName  {

	private static final long serialVersionUID = 2533238775751991883L;

	@Version
	private long version;

	@Id
	@GeneratedValue(generator = "person_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Enumerated(EnumType.ORDINAL)
	private Gender gender = Gender.UNKNOWN;

	@Column(columnDefinition = "VARCHAR")
	private String title = "";

	@Column(columnDefinition = "VARCHAR")
	private String lastName;

	@Column(columnDefinition = "VARCHAR")
	private String firstName;

	@Column(columnDefinition = "VARCHAR")
	private String birthName;

	@Column(columnDefinition = "VARCHAR")
	@Type(type = "date")
	private Date birthday;

	@Column(columnDefinition = "VARCHAR")
	private String language;

	@Column(columnDefinition = "VARCHAR")
	private String note;

	@OneToOne(cascade = CascadeType.ALL)
	private Contact contact;

	/**
	 * If true the persons data will no be updated with backend data, default true
	 */
	@Column(columnDefinition = "boolean default true")
	private boolean autoUpdate = true;

	@ManyToMany()
	@LazyCollection(FALSE)
	@JoinTable(name = "person_organization", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "organization_id"))
	private List<Organization> organizsations;
	
	/**
	 * Default address for notification
	 */
	@OneToOne
	private Organization defaultAddress;
	
	public enum Gender {
		MALE, FEMALE, UNKNOWN;
	}

	public Person() {
	}

	public Person(String name) {
		this(name, new Contact());
	}

	public Person(Contact contact) {
		this(null, contact);
	}

	public Person(String name, Contact contact) {
		this.lastName = name;
		this.contact = contact;
	}

	protected boolean archived;

	@Transient
	public String patienDataAsGson() {
		final GsonBuilder builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		final Gson gson = builder.create();
		return gson.toJson(this);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Person && ((Person) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}

	/********************************************************
	 * Interace archive able
	 ********************************************************/
	@Override
	public boolean isArchived() {
		return archived;
	}

	@Override
	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Override
	@Transient
	public String getTextIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transient
	public Dialog getArchiveDialog() {
		// TODO Auto-generated method stub
		return null;
	}
	/********************************************************
	 * Interace archive able
	 ********************************************************/

}
