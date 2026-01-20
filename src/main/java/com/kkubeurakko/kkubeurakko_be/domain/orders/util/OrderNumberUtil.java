package com.kkubeurakko.kkubeurakko_be.domain.orders.util;

import java.time.LocalDate;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreBusinessHoursService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderNumberUtil {
	private final StringRedisTemplate redisTemplate;
	private final StoreBusinessHoursService storeBusinessHoursService;

	public String createOrderNumber(Long storeId, OrderType orderType) {

		LocalDate businessDate =
			storeBusinessHoursService.resolveBusinessDate(storeId);

		String key = String.format(
			"order:number:%d:%s:%s",
			storeId,
			orderType.getCode(),
			businessDate
		);

		Long seq = redisTemplate.opsForValue().increment(key);

		return orderType.getCode() + "-" + seq;
	}
}
