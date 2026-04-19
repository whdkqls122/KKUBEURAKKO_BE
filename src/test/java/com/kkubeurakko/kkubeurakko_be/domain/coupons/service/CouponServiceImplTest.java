package com.kkubeurakko.kkubeurakko_be.domain.coupons.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.CouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.dto.IssuedCouponResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponCategory;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.CouponType;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.CouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.repository.UserCouponRepository;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

	@Mock
	UserService userService;
	@Mock
	CouponRepository couponRepository;
	@Mock
	UserCouponRepository userCouponRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	CouponServiceImpl couponService;

	Users testUser;
	Coupons coupon1, coupon2;

	@BeforeEach
	void setUp() {
		testUser = TestFixture.createUser("U123", "010-0000-0000");

		coupon1 = TestFixture.createCoupon(
			1L, "쿠폰1", 10,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(10),
			CouponType.DELIVERY, Status.OFFER,
			TestFixture.createStore()
		);

		coupon2 = TestFixture.createCoupon(
			2L, "쿠폰2", 5,
			LocalDate.now().minusDays(5),
			LocalDate.now().plusDays(5),
			CouponType.DELIVERY, Status.OFFER,
			TestFixture.createStore()
		);
	}

	@Test
	@DisplayName("유저가 보유하지 않은 쿠폰만 조회되는지 검증한다.")
	void testGetAllCouponDoesNotHave() {
		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");

		when(userService.getUser("U123")).thenReturn(testUser);
		when(couponRepository.findAllUserDoesNotHave("U123", Status.OFFER))
			.thenReturn(List.of(coupon1, coupon2));

		List<CouponResponseDto> result = couponService.getAllCouponDoesNotHave(oauthUser);

		assertThat(result).hasSize(2);
		verify(couponRepository).findAllUserDoesNotHave("U123", Status.OFFER);
	}

	@Test
	@DisplayName("유저가 보유한 쿠폰 목록 조회가 정상적으로 이루어지는지 검증한다.")
	void testGetAllUserOwned() {
		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");

		when(userService.getUser("U123")).thenReturn(testUser);

		UsersCoupons userCoupon = TestFixture.createUserCoupon(1L, testUser, coupon1, false);
		when(userCouponRepository.findAllUserOwned("U123"))
			.thenReturn(List.of(userCoupon));

		List<IssuedCouponResponseDto> result = couponService.getAllUserOwned(oauthUser);

		assertThat(result).hasSize(1);
		assertThat(result.get(0))
			.extracting("id", "name", "couponNumber")
			.containsExactly(userCoupon.getId(), coupon1.getName(), userCoupon.getCouponsNumber());

		verify(userCouponRepository).findAllUserOwned("U123");
	}

	@Test
	@DisplayName("쿠폰 발급이 정상적으로 이루어지는지 검증한다.")
	void testIssueCouponSuccessfully() {
		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");
		when(userService.getUser("U123")).thenReturn(testUser);
		when(userCouponRepository.existsByUserUserNumberAndCouponId("U123", 1L))
			.thenReturn(false);

		// PESSIMISTIC_WRITE 시 findByIdForUpdate 사용
		when(couponRepository.findByIdForUpdate(1L))
			.thenReturn(Optional.of(coupon1));

		couponService.issueCoupon(1L, oauthUser);

		assertThat(coupon1.getQuantity()).isEqualTo(9);
		verify(userCouponRepository).save(any(UsersCoupons.class));
	}

	@Test
	@DisplayName("이미 발급받은 쿠폰을 발급 받으려 할 때 예외가 발생하는지 검증한다.")
	void issueCouponAlreadyIssued() {
		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");
		when(userService.getUser("U123")).thenReturn(testUser);
		when(userCouponRepository.existsByUserUserNumberAndCouponId("U123", 1L))
			.thenReturn(true);
		when(couponRepository.findByIdForUpdate(1L))
			.thenReturn(Optional.of(coupon1));

		assertThatThrownBy(() -> couponService.issueCoupon(1L, oauthUser))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 발급받은 쿠폰입니다.");
	}

	@Test
	@DisplayName("쿠폰 사용 처리가 정상적으로 이루어지는지 검증한다.")
	void usedCoupon() {
		UsersCoupons userCoupon =
			TestFixture.createUserCoupon(1L, testUser, coupon1, false);

		when(userService.getUser("U123")).thenReturn(testUser);
		when(userCouponRepository.findByIdAndUser(1L, "U123"))
			.thenReturn(Optional.of(userCoupon));

		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");

		couponService.usedCoupon(1L, oauthUser);

		assertThat(userCoupon.isUsed()).isTrue();
	}

	@Test
	@DisplayName("동시에 여러 사용자가 쿠폰 발급을 요청해도 수량이 음수가 되지 않는지 검증한다.")
	void issueCouponConcurrency() throws Exception {

		// 쿠폰 수량 10개
		Coupons realCoupon = TestFixture.createCoupon(
			100L, "동시성쿠폰", 10,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(10),
			CouponType.DELIVERY, Status.OFFER,
			TestFixture.createStore()
		);

		when(couponRepository.findByIdForUpdate(100L))
			.thenReturn(Optional.of(realCoupon));

		when(userCouponRepository.existsByUserUserNumberAndCouponId(anyString(), eq(100L)))
			.thenReturn(false);

		CustomOAuth2User oauthUser = mock(CustomOAuth2User.class);
		when(oauthUser.getName()).thenReturn("U123");
		when(userService.getUser("U123")).thenReturn(testUser);

		// 동시 실행 20개 요청
		int threadCount = 20;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					couponService.issueCoupon(100L, oauthUser);
				} catch (Exception ignored) {
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		assertThat(realCoupon.getQuantity())
			.isBetween(0, 10);

		verify(userCouponRepository, atMost(10)).save(any(UsersCoupons.class));

		executor.shutdown();
	}

	@Test
	@DisplayName("할인 쿠폰이 정상적으로 생성된다")
	void createCoupon() {

		Stores store = Stores.builder().build();

		when(storeRepository.findById(1L))
			.thenReturn(Optional.of(store));

		when(couponRepository.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		CouponCreateRequest request = new CouponCreateRequest(
			"3000원 할인 쿠폰",
			3000,
			null,
			15000,
			CouponType.DELIVERY,
			CouponCategory.SURVEY_REWARD,
			LocalDate.now(),
			LocalDate.now().plusDays(7),
			100
		);

		couponService.createCoupon(1L, request);

		verify(couponRepository).save(any(Coupons.class));
	}

	@Test
	@DisplayName("가게에 등록된 쿠폰 목록을 조회한다")
	void getCouponsByStore() {
		// given
		Coupons coupon = Coupons.builder()
			.id(1L)
			.name("10% 할인 쿠폰")
			.discountRate(10)
			.minOrderAmount(20000)
			.couponType(CouponType.TAKEOUT)
			.validFrom(LocalDate.now())
			.validUntil(LocalDate.now().plusDays(10))
			.quantity(50)
			.status(Status.OFFER)
			.build();

		when(couponRepository.findByStoreId(1L))
			.thenReturn(List.of(coupon));

		// when
		var result = couponService.getCouponsByStore(1L);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).name()).isEqualTo("10% 할인 쿠폰");
		verify(couponRepository).findByStoreId(1L);
	}

	@Test
	@DisplayName("쿠폰 숨김 처리가 정상적으로 이루어진다.")
	void hideCouponSuccessfully() {
		// given
		Stores store = TestFixture.createStore();

		Coupons coupon = TestFixture.createCoupon(
			1L,
			"테스트 쿠폰",
			10,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(10),
			CouponType.DELIVERY,
			Status.OFFER,
			store
		);

		when(couponRepository.findById(1L))
			.thenReturn(Optional.of(coupon));

		// when
		couponService.hideCoupon(1L, store.getId());

		// then
		assertThat(coupon.getStatus()).isEqualTo(Status.HIDE);
	}

	@Test
	@DisplayName("숨김 처리된 쿠폰이 다시 활성화 된다.")
	void showCouponSuccessfully() {
		// given
		Stores store = TestFixture.createStore();

		Coupons coupon = TestFixture.createCoupon(
			1L,
			"테스트 쿠폰",
			10,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(10),
			CouponType.DELIVERY,
			Status.HIDE,
			store
		);

		when(couponRepository.findById(1L))
			.thenReturn(Optional.of(coupon));

		// when
		couponService.showCoupon(1L, store.getId());

		// then
		assertThat(coupon.getStatus()).isEqualTo(Status.OFFER);
	}
}
