package com.kkubeurakko.kkubeurakko_be.domain.orders.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kkubeurakko.kkubeurakko_be.domain.orders.config.OrderSocketSender;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreatedEvent;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCommitListener {
	private final OrderRepository orderRepository;
	private final OrderSocketSender orderSocketSender;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleOrderCreated(OrderCreatedEvent event) {
		try {
			Orders order = orderRepository.findById(event.orderId())
				.orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

			orderSocketSender.sendOrder(event.storeId(), order);

			log.info(
				"주문 커밋 후 매장 소켓 전송 성공 (orderId={}, storeId={})",
				event.orderId(),
				event.storeId()
			);
		} catch (Exception e) {
			log.error(
				"주문 커밋 후 매장 소켓 전송 실패 (orderId={}, storeId={})",
				event.orderId(),
				event.storeId(),
				e
			);
		}
	}
}
