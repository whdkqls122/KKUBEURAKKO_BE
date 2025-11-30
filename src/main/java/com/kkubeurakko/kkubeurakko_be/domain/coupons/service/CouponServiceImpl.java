package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {
	private final UserService userService;
	private final CouponRepository couponRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CouponResponseDto> getAllCouponDoesNotHave(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Coupons> couponList = couponRepository.findAllUserDoesNotHave(user.getUserNumber());
		return toResponseDto(couponList);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CouponResponseDto> getAllCouponDoesHave(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Coupons> couponsList = couponRepository.findAllUserOwned(user.getUserNumber());
		return toResponseDto(couponsList);
	}

	private List<CouponResponseDto> toResponseDto(List<Coupons> couponList) {
		return couponList.stream()
			.map(c -> CouponResponseDto.builder()
				.id(c.getId())
				.name(c.getName())
				.discountAmount(c.getDiscountAmount())
				.minOrderAmount(c.getMinOrderAmount())
				.couponType(c.getCouponType())
				.validFrom(c.getValidFrom())
				.validUntil(c.getValidUntil())
				.quantity(c.getQuantity())
				.status(c.getStatus())
				.build()
			)
			.toList();
	}
}
