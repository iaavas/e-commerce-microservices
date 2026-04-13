package com.ecommerce.inventory.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryOrderEventsConsumer {

	private static final Logger log = LoggerFactory.getLogger(InventoryOrderEventsConsumer.class);

	@KafkaListener(topics = "order.placed")
	public void onOrderPlaced(OrderPlacedEvent event) {
		if (event.items() == null) {
			return;
		}
		event.items().forEach(item -> log.info("Async inventory deduction: orderId={}, productId={}, quantity={}",
				event.orderId(), item.productId(), item.quantity()));
	}
}
