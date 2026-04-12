package com.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.gateway.discovery.locator.enabled=false",
		"jwt.secret=test-secret-key-must-be-at-least-32-bytes-long!!"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}
}
