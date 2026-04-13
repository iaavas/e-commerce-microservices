package com.ecommerce.product.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.repo.CategoryRepository;
import com.ecommerce.product.repo.ProductRepository;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "eureka.client.enabled=false")
class ProductApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductService productService;

	@Autowired
	private ObjectMapper objectMapper;

	private Long categoryId;

	@BeforeEach
	void setup() {
		Category c = new Category();
		c.setName("Electronics");
		categoryId = categoryRepository.save(c).getId();
	}

	@Test
	void listIsPublicAndEmptyInitially() throws Exception {
		mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void adminCrudAndPublicFilters() throws Exception {
		String createJson = """
				{"name":"Phone","description":"Smart","price":99.99,"stockQuantity":10,"categoryId":%d,"imageUrl":"https://example.com/p.png"}
				""".formatted(categoryId);

		String created = mockMvc
				.perform(post("/api/admin/products").with(jwt()).contentType(APPLICATION_JSON).content(createJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Phone"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		long productId = objectMapper.readTree(created).get("id").asLong();

		mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/api/products").param("category", String.valueOf(categoryId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/api/products").param("category", "99999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(get("/api/products/{id}", productId)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Phone"));

		String updateJson = """
				{"name":"Phone Pro","description":"Smarter","price":129.99,"stockQuantity":5,"categoryId":%d,"imageUrl":"https://example.com/p2.png"}
				""".formatted(categoryId);
		mockMvc.perform(put("/api/admin/products/{id}", productId).with(jwt()).contentType(APPLICATION_JSON).content(updateJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Phone Pro"));

		mockMvc.perform(delete("/api/admin/products/{id}", productId).with(jwt())).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/products/{id}", productId)).andExpect(status().isNotFound());
	}

	@Test
	void adminRequiresAuthentication() throws Exception {
		String createJson = """
				{"name":"X","description":"","price":1.0,"stockQuantity":1,"categoryId":%d,"imageUrl":null}
				""".formatted(categoryId);
		mockMvc.perform(post("/api/admin/products").contentType(APPLICATION_JSON).content(createJson))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deductStockRespectsQuantityAndVersion() {
		Category cat = categoryRepository.findById(categoryId).orElseThrow();
		Product p = new Product();
		p.setName("Widget");
		p.setDescription("d");
		p.setPrice(BigDecimal.TEN);
		p.setStockQuantity(2);
		p.setCategory(cat);
		final Product saved = productRepository.save(p);

		productService.deductStock(saved.getId(), 1);
		Product updated = productRepository.findById(saved.getId()).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(updated.getStockQuantity()).isEqualTo(1);

		assertThatThrownBy(() -> productService.deductStock(saved.getId(), 5)).isInstanceOf(ResponseStatusException.class);
	}
}
