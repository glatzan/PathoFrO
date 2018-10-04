package com.patho.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Patient;

@Service
@Transactional
public class FileService {

	public void addFileToPatient(String piz, PDFContainer container) {
		
	}
	
	public void addFileToPatient(Patient patient, PDFContainer container) {
		
	}
}
