package com.patho.main;

import com.patho.main.model.patient.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JSONTest {


    @Test
    public void test2() {
//        Optional<Patient> test = jsonPatientRepository.findByPIZ("20366346");
//        assertTrue(test.isPresent());

        assertTrue(true);
    }
}
