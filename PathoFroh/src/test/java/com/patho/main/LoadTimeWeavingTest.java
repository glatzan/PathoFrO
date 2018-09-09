package com.patho.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.patho.main.test.TestComponent;
import com.patho.main.test.TestHandler;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadTimeWeavingTest {

	@Autowired
	private TestHandler testHandler = null;

	@Test
	public void testWeavingFromContext() {
		assertNotNull("Processor is null.", testHandler);
	}

	@Test
	public void testWeavingFromConfigurable() {
		TestComponent c = new TestComponent();
		System.out.println(c.testHandler);
		assertNotNull("Processor is null.", c.testHandler);
	}
}
