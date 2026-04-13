package com.ecommerce.order.repo;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@EntityGraph(attributePaths = "items")
	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	@EntityGraph(attributePaths = "items")
	java.util.Optional<Order> findById(Long id);
}
