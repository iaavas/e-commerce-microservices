package com.ecommerce.product.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByCategory_Id(Long categoryId);
}
