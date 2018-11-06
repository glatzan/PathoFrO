package com.patho.main.model.dto.ldap;

import java.util.HashSet;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import com.patho.main.model.Contact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;

import lombok.Getter;
import lombok.Setter;

@Entry(objectClasses = { "person", "fw1Person", "top", "uklfrPerson" }, base = "ou=people")
@Getter
@Setter
public final class LDAPUserMapper {

	@Id
	private Name dn;

	/**
	 * Full name
	 */
	@Attribute(name="cn")
	private String fullName;

	/**
	 * Title e.g. Dr.
	 */
	@Attribute(name="personalTitle")
	private String title;

	/**
	 * Login name
	 */
	@Attribute(name="uid")
	private String uid;

	/**
	 * last name
	 */
	@Attribute(name="sn")
	private String lastName;

	/**
	 * Employee Number
	 */
	@Attribute(name="employeeNumber")
	private String employeeNumber;

	/**
	 * First name
	 */
	@Attribute(name="givenName")
	private String firstName;

	/**
	 * Email
	 */
	@Attribute(name="mail")
	private String mail;

	/**
	 * phone
	 */
	@Attribute(name="telephonenumber")
	private String phone;

	/**
	 * PAger
	 */
	@Attribute(name="pager")
	private String pager;

	/**
	 * Clinical Role. e.g. consultant
	 */
	@Attribute(name="title")
	private String clinicRole;

	/**
	 * Name of Organization
	 */
	@Attribute(name="ou")
	private String organization;

	/**
	 * SEX, 1 = male, > 1 female
	 */
	@Attribute(name="uklfrPersonType")
	private String sex;

	public Physician getPhysician() {
		return getPhysician(new Physician(new Person(new Contact())));
	}

	public Physician getPhysician(Physician physician) {
		physician.getPerson().setTitle(getTitle());
		physician.setUid(getUid());
		physician.getPerson().setLastName(getLastName());
		physician.setEmployeeNumber(getEmployeeNumber());
		physician.getPerson().setFirstName(getFirstName());
		physician.getPerson().getContact().setEmail(getMail());
		physician.getPerson().getContact().setPhone(getPhone());
		physician.getPerson().getContact().setPager(getPager());
		physician.setClinicRole(getClinicRole());
		physician.getPerson().setGender(getSex().equals("1") ? Person.Gender.MALE : Person.Gender.FEMALE);

		if (physician.getPerson().getOrganizsations() == null)
			physician.getPerson().setOrganizsations(new HashSet<Organization>());

		Organization organization = new Organization(getOrganization(), new Contact(), true);
		if (!physician.getPerson().getOrganizsations().stream()
				.anyMatch(p -> organization.getName().equals(p.getName())))
			physician.getPerson().getOrganizsations().add(organization);

		return physician;
	}
}
