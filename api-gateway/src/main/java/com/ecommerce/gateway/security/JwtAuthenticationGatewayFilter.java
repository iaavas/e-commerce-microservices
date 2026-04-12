package com.ecommerce.gateway.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

	private final GatewayJwtProperties jwtProperties;

	public JwtAuthenticationGatewayFilter(GatewayJwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		if (isPublicPath(path)) {
			return chain.filter(exchange);
		}
		String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		String token = header.substring(7).trim();
		try {
			Claims claims = parseClaims(token);
			Object uid = claims.get("uid");
			ServerHttpRequest mutated = exchange.getRequest().mutate()
					.header("X-User-Id", uid != null ? uid.toString() : "")
					.header("X-User-Email", claims.getSubject() != null ? claims.getSubject() : "")
					.build();
			return chain.filter(exchange.mutate().request(mutated).build());
		}
		catch (JwtException | IllegalArgumentException e) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey signingKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private static boolean isPublicPath(String path) {
		if (path.startsWith("/actuator")) {
			return true;
		}
		if ("/auth/register".equals(path) || "/auth/login".equals(path)) {
			return true;
		}
		return "/auth-service/auth/register".equals(path) || "/auth-service/auth/login".equals(path);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}
}
