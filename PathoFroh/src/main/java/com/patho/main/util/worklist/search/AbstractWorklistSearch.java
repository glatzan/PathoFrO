package com.patho.main.util.worklist.search;

import com.patho.main.model.patient.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AbstractWorklistSearch {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public List<Patient> getPatients() {
		return new ArrayList<Patient>();
	}
}
