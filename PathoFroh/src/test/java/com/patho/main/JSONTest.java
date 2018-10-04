package com.patho.main;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.patho.main.model.patient.Patient;
import com.patho.main.repository.JSONPatientRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JSONTest {

	private JSONPatientRepository jsonPatientRepository;

	@Test
	public void test2() {
		Optional<Patient> test = jsonPatientRepository.findByPIZ("20366346");
		assertTrue(test.isPresent());
		
	}
}
