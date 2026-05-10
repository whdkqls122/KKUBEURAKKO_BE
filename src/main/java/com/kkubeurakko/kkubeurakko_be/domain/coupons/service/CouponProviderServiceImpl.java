package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.util.GeneratedCouponNumberUtil;
import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponProviderServiceImpl implements CouponProviderService {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;
	private final GeneratedCouponNumberUtil generatedCouponNumberUtil;
	private final UserRepository userRepository;

	@Override
	public void provideSurveyRewardCoupon(Users user, Survey survey) {
		Coupons coupon = couponRepository.findByCouponCategoryAndStatus(CouponCategory.SURVEY_REWARD, Status.OFFER)
			.orElseThrow(() -> new IllegalArgumentException("설문 조사 보상 쿠폰이 없습니다."));

		if (!validateCoupon(user, coupon)) {
			return;
		}

		coupon.updateQuantity();

		String couponNumber = generatedCouponNumberUtil.generatedCouponNumber();

		UsersCoupons usersCoupons = UsersCoupons.builder()
			.coupon(coupon)
			.user(user)
			.couponsNumber(couponNumber)
			.isUsed(false)
			.build();

		userCouponRepository.save(usersCoupons);
		log.info("설문 보상 쿠폰 지급 완료");
	}

	@Override
	@Transactional
	public void provideGiftCoupon() {

		List<Coupons> coupons = couponRepository
			.findAllByCouponCategoryAndStatus(
				CouponCategory.GIFT,
				Status.OFFER
			);

		if (coupons.isEmpty()) {
			return;
		}

		for (Coupons coupon : coupons) {

			List<Users> users =
				userRepository.findUsersWithoutCoupon(coupon.getId());

			if (users.isEmpty()) {
				continue;
			}

			int issueCount = users.size();

			if (coupon.getQuantity() < issueCount) {
				throw new IllegalArgumentException(
					coupon.getName() + " 수량 부족"
				);
			}

			List<UsersCoupons> userCoupons = users.stream()
				.map(user -> UsersCoupons.builder()
					.coupon(coupon)
					.user(user)
					.couponsNumber(
						generatedCouponNumberUtil.generatedCouponNumber()
					)
					.isUsed(false)
					.build())
				.toList();

			IntStream.range(0, issueCount)
				.forEach(i -> coupon.updateQuantity());

			userCouponRepository.saveAll(userCoupons);
		}

		log.info("GIFT 쿠폰 지급 완료");
	}

	private boolean validateCoupon(Users user, Coupons coupon) {
		boolean provided = userCouponRepository.existsByUserUserNumberAndCouponId(user.getUserNumber(), coupon.getId());

		if (provided) {
			log.warn("이미 설문 보상 쿠폰이 지급된 사용자입니다.");
			return false;
		}

		if (coupon.getQuantity() <= 0) {
			throw new IllegalArgumentException("설문 보상 쿠폰 수량이 부족합니다.");
		}

		return true;
	}
}
