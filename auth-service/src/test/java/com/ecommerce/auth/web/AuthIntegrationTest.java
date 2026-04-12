package com.ecommerce.auth.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "eureka.client.enabled=false")
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void registerThenAccessMeWithBearerToken() throws Exception {
		String email = "u" + UUID.randomUUID() + "@test.com";
		String registerBody = "{\"email\":\"" + email + "\",\"password\":\"password12\"}";
		MvcResult created = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode root = objectMapper.readTree(created.getResponse().getContentAsString());
		String token = root.get("accessToken").asText();

		mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email.toLowerCase()))
				.andExpect(jsonPath("$.userId").exists());
	}

	@Test
	void meWithoutTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateRegisterReturnsConflict() throws Exception {
		String email = "dup" + UUID.randomUUID() + "@test.com";
		String body = "{\"email\":\"" + email + "\",\"password\":\"password12\"}";
		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
	}
}
