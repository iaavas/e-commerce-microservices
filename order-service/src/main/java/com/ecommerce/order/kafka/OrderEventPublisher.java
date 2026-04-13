package com.ecommerce.order.kafka;

import java.util.List;

import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ecommerce.order.domain.Order;

@Component
public class OrderEventPublisher {

	private static final String ORDER_PLACED_TOPIC = "order.placed";

	private final KafkaOperations<String, Object> kafkaTemplate;

	public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishOrderPlaced(Order order) {
		List<OrderItemEvent> items = order.getItems().stream()
				.map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
				.toList();
		OrderPlacedEvent event = new OrderPlacedEvent(order.getId(), order.getUserId(), order.getTotalAmount(),
				order.getCreatedAt(), items);
		kafkaTemplate.send(ORDER_PLACED_TOPIC, order.getId().toString(), event);
	}
}
