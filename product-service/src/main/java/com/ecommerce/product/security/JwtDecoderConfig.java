package com.ecommerce.product.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtDecoderConfig {

	@Bean
	JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
		return NimbusJwtDecoder.withSecretKey(secretKey).build();
	}
}
