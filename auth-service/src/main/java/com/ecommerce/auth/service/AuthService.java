package com.ecommerce.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.repo.UserRepository;
import com.ecommerce.auth.security.JwtProperties;
import com.ecommerce.auth.security.JwtService;
import com.ecommerce.auth.web.dto.AuthResponse;
import com.ecommerce.auth.web.dto.LoginRequest;
import com.ecommerce.auth.web.dto.RegisterRequest;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String normalized = request.getEmail().trim().toLowerCase();
		if (userRepository.existsByEmail(normalized)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		User user = new User();
		user.setEmail(normalized);
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered", ex);
		}
		String token = jwtService.generateToken(user);
		return toResponse(token);
	}

	public AuthResponse login(LoginRequest request) {
		String normalized = request.getEmail().trim().toLowerCase();
		User user = userRepository.findByEmail(normalized)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		return toResponse(jwtService.generateToken(user));
	}

	private AuthResponse toResponse(String token) {
		return new AuthResponse(token, "Bearer", jwtProperties.getExpirationMs() / 1000);
	}
}
