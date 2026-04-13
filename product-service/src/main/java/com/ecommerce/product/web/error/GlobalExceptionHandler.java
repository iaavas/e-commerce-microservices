package com.ecommerce.product.web.error;

import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(OptimisticLockingFailureException.class)
	ResponseEntity<Map<String, String>> onOptimisticLock(OptimisticLockingFailureException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", "Concurrent update", "message", "Retry the operation"));
	}
}
