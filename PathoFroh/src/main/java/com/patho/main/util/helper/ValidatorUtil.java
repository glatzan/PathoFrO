package com.patho.main.util.helper;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;

@Configurable
public class ValidatorUtil {

	@Autowired
	private PathoConfig pathoConfig;

	public boolean approveMailAddress(String address) {
		return (HistoUtil.isNotNullOrEmpty(address) && !EmailValidator.getInstance().isValid(address));
	}

	public boolean approveFaxAddress(String address) {
		if (!HistoUtil.isNotNullOrEmpty(address) || !address.matches(pathoConfig.getMiscellaneous().getPhoneRegex()))
			return false;

		return true;
	}

	public boolean approvePostalAddress(String address) {
		if (!HistoUtil.isNotNullOrEmpty(address))
			return false;
		return true;
	}

}