package com.ecommerce.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "eureka.client.enabled=false")
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
