package com.kkubeurakko.kkubeurakko_be.domain.orders.dto;

public record OrderCreatedEvent(Long orderId, Long storeId) {
}