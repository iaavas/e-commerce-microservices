package com.ecommerce.notification.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationOrderEventsConsumer {

	private static final Logger log = LoggerFactory.getLogger(NotificationOrderEventsConsumer.class);

	@KafkaListener(topics = "order.placed")
	public void onOrderPlaced(OrderPlacedEvent event) {
		log.info("Sending confirmation email for orderId={} userId={} amount={}",
				event.orderId(), event.userId(), event.totalAmount());
	}
}
