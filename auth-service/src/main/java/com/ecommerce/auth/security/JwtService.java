package com.ecommerce.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.ecommerce.auth.domain.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public String generateToken(User user) {
		long now = System.currentTimeMillis();
		Date issued = new Date(now);
		Date expiry = new Date(now + jwtProperties.getExpirationMs());
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("uid", user.getId())
				.issuedAt(issued)
				.expiration(expiry)
				.signWith(signingKey())
				.compact();
	}

	private SecretKey signingKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
