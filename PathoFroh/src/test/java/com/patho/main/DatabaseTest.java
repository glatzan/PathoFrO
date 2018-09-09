package com.patho.main;

import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseTest {
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	UserRepository userRepository;  
	
	@Test
	public void test1() throws SQLException {
		assertNotNull("Processor is null.", dataSource.getConnection());
	}
	
	@Test
	public void test2() {
		Optional<HistoUser> user = userRepository.findById((long) 1);
		assertNotNull("Processor is null.", user.get());
	}
}
