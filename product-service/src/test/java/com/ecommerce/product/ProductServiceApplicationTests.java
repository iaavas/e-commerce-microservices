package com.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "eureka.client.enabled=false")
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
