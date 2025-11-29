package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@RequestMapping("/v1/coupon")
public class CouponServiceImpl implements CouponService {
	private final UserService userService;
	private final CouponRepository couponRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CouponResponseDto> getAllCoupon(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Coupons> couponList = couponRepository.findAllUserDoesNotHave(user);
		return toResponseDto(couponList);
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
