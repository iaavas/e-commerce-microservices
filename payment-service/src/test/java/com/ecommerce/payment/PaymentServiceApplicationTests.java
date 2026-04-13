package com.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "eureka.client.enabled=false")
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
