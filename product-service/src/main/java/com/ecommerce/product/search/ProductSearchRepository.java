package com.ecommerce.product.search;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

	List<ProductDocument> findByNameContainingOrDescriptionContaining(String nameTerm, String descriptionTerm);
}
