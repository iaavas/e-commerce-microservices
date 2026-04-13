package com.ecommerce.product.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.CategoryRepository;
import com.ecommerce.product.repo.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "eureka.client.enabled=false")
class CartApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	private Long productId;

	@BeforeEach
	void setup() {
		Category category = new Category();
		category.setName("Accessories");
		Category savedCategory = categoryRepository.save(category);

		Product product = new Product();
		product.setName("Mouse");
		product.setDescription("Wireless mouse");
		product.setPrice(new BigDecimal("25.50"));
		product.setStockQuantity(50);
		product.setCategory(savedCategory);
		productId = productRepository.save(product).getId();
	}

	@Test
	void cartRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
	}

	@Test
	void addUpdateRemoveAndGetCart() throws Exception {
		mockMvc.perform(post("/api/cart/add")
				.with(jwt().jwt(jwt -> jwt.claim("uid", 101L)))
				.contentType(APPLICATION_JSON)
				.content("""
						{"productId": %d, "quantity": 2}
						""".formatted(productId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].productId").value(productId))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].lineTotal").value(51.0))
				.andExpect(jsonPath("$.totalPrice").value(51.0));

		mockMvc.perform(put("/api/cart/update/{itemId}", productId)
				.with(jwt().jwt(jwt -> jwt.claim("uid", 101L)))
				.contentType(APPLICATION_JSON)
				.content("""
						{"quantity": 3}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.items[0].lineTotal").value(76.5))
				.andExpect(jsonPath("$.totalPrice").value(76.5));

		mockMvc.perform(get("/api/cart").with(jwt().jwt(jwt -> jwt.claim("uid", 101L))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].productId").value(productId))
				.andExpect(jsonPath("$.items[0].quantity").value(3));

		mockMvc.perform(delete("/api/cart/remove/{itemId}", productId).with(jwt().jwt(jwt -> jwt.claim("uid", 101L))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.totalPrice").value(0));
	}
}
