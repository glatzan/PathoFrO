package com.patho.main.util.helper;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.patho.main.model.patient.Diagnosis;
import com.patho.main.util.exception.CustomNotUniqueReqest;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UniqueRequestID {

	private final static String SUBMITTEDID = "submittedRequestID";

	private SecureRandom random = new SecureRandom();

	@Getter
	@Setter
	private String uniqueRequestID;

	@Getter
	@Setter
	private String submittedRequestID;

	@Getter
	@Setter
	private boolean enabled;

	public UniqueRequestID() {
		setUniqueRequestID("");
	}

	public void nextUniqueRequestID() {
		setUniqueRequestID(new BigInteger(130, random).toString(32));
		log.debug("New Unique ID generated");
	}

	public void checkUniqueRequestID(boolean toClose) {
		// disables uniqueCheck
		checkUniqueRequestID(toClose, false);
	}

	public void checkUniqueRequestID(boolean toClose, boolean enabled) {

		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		String submittedRequestID = params.get(SUBMITTEDID);

		if (submittedRequestID == null || submittedRequestID.isEmpty()) {
			log.debug("No ID submitted");
			throw new CustomNotUniqueReqest(toClose);
		}

		if (uniqueRequestID.isEmpty() || !uniqueRequestID.equals(submittedRequestID)) {
			log.debug("ID does not equal");
			throw new CustomNotUniqueReqest(toClose);
		}

		log.debug("Unique ID matched");

		setUniqueRequestID("");
		setEnabled(enabled);
	}
}
