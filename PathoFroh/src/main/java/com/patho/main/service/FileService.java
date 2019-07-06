package com.patho.main.service;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Patient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileService {

	public void addFileToPatient(String piz, PDFContainer container) {
		
	}
	
	public void addFileToPatient(Patient patient, PDFContainer container) {
		
	}
}
