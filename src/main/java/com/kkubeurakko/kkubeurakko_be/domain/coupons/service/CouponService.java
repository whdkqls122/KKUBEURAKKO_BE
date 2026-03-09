package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.AdminCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.IssuedCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface CouponService {
	//쿠폰 목록 조회
	List<CouponResponseDto> getAllCouponDoesNotHave(CustomOAuth2User customOAuth2User);

	//보유 쿠폰 조회
	List<IssuedCouponResponseDto> getAllUserOwned(CustomOAuth2User customOAuth2User);

	//쿠폰 발급
	void issueCoupon(Long couponId, CustomOAuth2User customOAuth2User);

	//쿠폰 사용
	void usedCoupon(Long userCouponId, CustomOAuth2User customOAuth2User);

	//가게측 쿠폰 발행
	void createCoupon(Long storeId, CouponCreateRequest request);

	//가게측 쿠폰 전체 조회
	List<AdminCouponResponseDto> getCouponsByStore(Long storeId);

	//쿠폰 숨김 설정
	void hideCoupon(Long couponId, Long storeId);

	//쿠폰 활성화 설정
	void showCoupon(Long couponId, Long storeId);
}
