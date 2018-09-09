package com.patho.main.util.worklist.search;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patho.main.model.patient.Patient;

import lombok.extern.slf4j.Slf4j;

public class AbstractWorklistSearch {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public List<Patient> getPatients() {
		return new ArrayList<Patient>();
	}
}
