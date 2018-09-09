package com.patho.main.model.interfaces;

import javax.persistence.Transient;

import com.patho.main.model.Person.Gender;

/**
 * Interface for returning a merged name from first, last name and title.
 * 
 * @author andi
 *
 */
public interface FullName  {

	public String getTitle();

	public String getFirstName();

	public String getLastName();

	public default Gender getGender() {
		return Gender.UNKNOWN;
	}

	/**
	 * Returns a full name with title, name and surname.
	 * 
	 * @return
	 */
	@Transient
	public default String getFullName() {
		StringBuilder result = new StringBuilder();

		if (getTitle() != null && !getTitle().isEmpty())
			result.append(getTitle() + " ");

		if (getFirstName() != null && !getFirstName().isEmpty())
			result.append(getFirstName() + " ");

		if (getLastName() != null && !getLastName().isEmpty())
			result.append(getLastName() + " ");

		// remove the last space from the string
		if (result.length() > 0)
			return result.substring(0, result.length() - 1);
		else
			return "";
	}

	/**
	 * Returns a title + name, if no title is provided,
	 * 
	 * @return
	 */
	@Transient
	public default String getFullNameAndTitle() {
		StringBuilder result = new StringBuilder();

		if (getTitle() != null && !getTitle().isEmpty())
			result.append(getTitle() + " ");
		else {
			// TODO hardcoded!
			if (getGender() == Gender.FEMALE)
				result.append("Frau ");
			else
				result.append("Herr ");
		}

		int index = result.indexOf("Apl.");
		if (index != -1)
			result.replace(index, 4, "");

		if (getLastName() != null && !getLastName().isEmpty())
			result.append(getLastName() + " ");

		return result.substring(0, result.length() - 1);
	}
}
