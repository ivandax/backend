package com.backend.demo;

import com.backend.demo.service.mailing.ResendEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class BackendApplicationTests {

	@MockBean
	private ResendEmailService resendEmailService;
	@Test
	void contextLoads() {
	}

}
