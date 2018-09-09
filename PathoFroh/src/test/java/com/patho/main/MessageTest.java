package com.patho.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;

import com.patho.main.config.util.ResourceBundle;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageTest {

	@Autowired
	private ResourceBundle resourceBundle;

	@Autowired
	private MessageSource messageSource;

	@Test
	public void testResourceBundle() {
		assertNotNull("Processor is null.", resourceBundle);
	}

	@Test
	public void testMessageSource() {
		assertNotNull("Processor is null.", messageSource);
	}

	@Test
	public void testMessagesLoaded() {
		assertEquals(resourceBundle.get("general.ok"), "OK");
	}
}
