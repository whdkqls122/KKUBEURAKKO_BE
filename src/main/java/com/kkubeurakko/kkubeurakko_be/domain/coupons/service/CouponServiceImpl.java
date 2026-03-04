package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.AdminCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.IssuedCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.exception.DuplicatedCouponNumberException;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
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
	private final UserCouponRepository userCouponRepository;
	private final StoreRepository storeRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CouponResponseDto> getAllCouponDoesNotHave(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<Coupons> couponList = couponRepository.findAllUserDoesNotHave(user.getUserNumber(), Status.OFFER);

		log.info("쿠폰 조회 성공");
		return toResponseDto(couponList);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IssuedCouponResponseDto> getAllUserOwned(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		List<UsersCoupons> couponsList = userCouponRepository.findAllUserOwned(user.getUserNumber());

		log.info("보유 쿠폰 조회 성공");
		return toIssuedResponseDto(couponsList);
	}

	@Override
	@Transactional
	public void issueCoupon(Long couponId, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		Coupons coupon = couponRepository.findByIdForUpdate(couponId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 쿠폰이 없습니다."));

		boolean alreadyIssued = userCouponRepository.existsByUserUserNumberAndCouponId(
			user.getUserNumber(), coupon.getId()
		);
		if (alreadyIssued) {
			throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
		}

		coupon.updateQuantity();

		try {
			UsersCoupons userCoupon = UsersCoupons.builder()
				.coupon(coupon)
				.user(user)
				.couponsNumber(generatedCouponNumber())
				.isUsed(false)
				.build();

			log.info("쿠폰 발급 성공");
			userCouponRepository.save(userCoupon);
		} catch (IllegalStateException e) {
			throw new DuplicatedCouponNumberException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public void usedCoupon(Long userCouponId, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		UsersCoupons userCoupon = userCouponRepository.findByIdAndUser(userCouponId, user.getUserNumber())
			.orElseThrow(() -> new IllegalArgumentException("조회된 쿠폰이 없습니다."));

		log.info("쿠폰 사용 성공");
		userCoupon.updateIsUsed();
	}

	@Override
	public void createCoupon(Long storeId, CouponCreateRequest request) {

		Stores store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

		validateDiscount(request);

		Coupons coupon = Coupons.builder()
			.name(request.name())
			.discountAmount(request.discountAmount())
			.discountRate(request.discountRate())
			.minOrderAmount(request.minOrderAmount())
			.couponType(request.couponType())
			.validFrom(request.validFrom())
			.validUntil(request.validUntil())
			.quantity(request.quantity())
			.status(Status.OFFER)
			.store(store)
			.build();

		couponRepository.save(coupon);

	}

	@Override
	public List<AdminCouponResponseDto> getCouponsByStore(Long storeId) {

		return couponRepository.findByStoreId(storeId)
			.stream()
			.map(this::toResponseAdminDto)
			.toList();
	}

	@Override
	@Transactional
	public void hideCoupon(Long couponId, Long storeId) {

		Coupons coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

		if (!coupon.getStore().getId().equals(storeId)) {
			throw new IllegalArgumentException("해당 매장의 쿠폰이 아닙니다.");
		}

		coupon.hide();
	}

	private void validateDiscount(CouponCreateRequest request) {
		if (request.discountAmount() == null && request.discountRate() == null) {
			throw new IllegalArgumentException("할인 금액 또는 할인율 중 하나는 필수입니다.");
		}
		if (request.discountAmount() != null && request.discountRate() != null) {
			throw new IllegalArgumentException("할인 금액과 할인율은 동시에 설정할 수 없습니다.");
		}
	}

	private List<CouponResponseDto> toResponseDto(List<Coupons> couponList) {
		return couponList.stream()
			.map(c -> CouponResponseDto.builder()
				.id(c.getId())
				.name(c.getName())
				.discountAmount(c.getDiscountAmount())
				.discountRate(c.getDiscountRate())
				.minOrderAmount(c.getMinOrderAmount())
				.couponType(c.getCouponType())
				.validFrom(c.getValidFrom())
				.validUntil(c.getValidUntil())
				.quantity(c.getQuantity())
				.status(c.getStatus())
				.build()
			)
			.collect(Collectors.toList());
	}

	private AdminCouponResponseDto toResponseAdminDto(Coupons coupon) {
		return AdminCouponResponseDto.builder()
			.couponId(coupon.getId())
			.name(coupon.getName())
			.discountAmount(coupon.getDiscountAmount())
			.discountRate(coupon.getDiscountRate())
			.minOrderAmount(coupon.getMinOrderAmount())
			.couponType(coupon.getCouponType())
			.validFrom(coupon.getValidFrom())
			.validUntil(coupon.getValidUntil())
			.quantity(coupon.getQuantity())
			.status(coupon.getStatus())
			.build();
	}

	private List<IssuedCouponResponseDto> toIssuedResponseDto(List<UsersCoupons> couponList) {
		return couponList.stream()
			.map(
				uc -> IssuedCouponResponseDto.builder()
					.id(uc.getCoupon().getId())
					.name(uc.getCoupon().getName())
					.couponNumber(uc.getCouponsNumber())
					.discountAmount(uc.getCoupon().getDiscountAmount())
					.discountRate(uc.getCoupon().getDiscountRate())
					.minOrderAmount(uc.getCoupon().getMinOrderAmount())
					.couponType(uc.getCoupon().getCouponType())
					.validFrom(uc.getCoupon().getValidFrom())
					.validUntil(uc.getCoupon().getValidUntil())
					.status(uc.getCoupon().getStatus())
					.build()
			)
			.collect(Collectors.toList());
	}

	private String generatedCouponNumber() {
		int maxAttempts = 10;
		for (int i = 0; i < maxAttempts; i++) {
			String couponNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 19);
			if (!userCouponRepository.existsByCouponsNumber(couponNumber)) {
				return couponNumber;
			}
		}
		throw new IllegalStateException("다시 요청 해주세요");
	}
}
