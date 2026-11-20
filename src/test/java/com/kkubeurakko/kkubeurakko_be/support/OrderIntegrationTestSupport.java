package com.kkubeurakko.kkubeurakko_be.support;

import static org.mockito.BDDMockito.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuGroupRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.OrderType;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

/**
 * 주문을 만드는 통합 테스트의 공통 데이터 준비
 * 스프링 설정은 IntegrationTestSupport와 같아서 같은 컨텍스트를 재사용
 */
public abstract class OrderIntegrationTestSupport extends IntegrationTestSupport {

	protected static final int MENU_PRICE = 18000;
	// AdminInitializer가 앱 기동 시 만드는 가게
	protected static final long STORE_ID = 1L;

	@Autowired
	protected UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private MenuGroupRepository menuGroupRepository;
	@Autowired
	private MenuRepository menuRepository;
	@Autowired
	private AesUtil aesUtil;
	@Autowired
	private PlatformTransactionManager transactionManager;

	protected TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUpTransactionTemplate() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	protected String newPaymentId() {
		return "it-" + UUID.randomUUID().toString().substring(0, 12);
	}

	protected String newPhone() {
		return String.format("010%08d", ThreadLocalRandom.current().nextInt(100_000_000));
	}

	protected Users saveUserWithCart() {
		return saveUserWithCart(newPhone());
	}

	/** 메뉴 1개(18,000원)가 담긴 장바구니를 가진 사용자를 저장. 전화번호는 운영과 같이 암호화해 저장 */
	protected Users saveUserWithCart(String phone) {
		Stores store = storeRepository.findById(STORE_ID).orElseThrow();
		MenuGroups menuGroup = menuGroupRepository.save(MenuGroups.builder()
			.name("통합 테스트 메뉴 그룹")
			.menuStatus(MenuStatus.ON_SALE)
			.store(store)
			.build());
		Menus menu = menuRepository.save(Menus.builder()
			.name("통합 테스트 메뉴")
			.menuStatus(MenuStatus.ON_SALE)
			.price(MENU_PRICE)
			.menuGroup(menuGroup)
			.build());

		Cart cart = Cart.builder().build();
		cart.getCartItemList().add(CartItem.builder().cart(cart).menu(menu).quantity(1).build());

		String unique = UUID.randomUUID().toString().substring(0, 8);
		return userRepository.save(Users.builder()
			.userNumber("it-user-" + unique)
			.phone(aesUtil.encryptToBase64(phone))
			.phoneHash("it-hash-" + unique)
			.nickname("통합테스트")
			.name("통합테스트")
			.role(UserRole.CUSTOMER)
			.cart(cart)
			.build());
	}

	protected CustomOAuth2User loginAs(Users user) {
		CustomOAuth2User customOAuth2User = Mockito.mock(CustomOAuth2User.class);
		given(customOAuth2User.getName()).willReturn(user.getUserNumber());
		return customOAuth2User;
	}

	protected OrderCreateRequest takeoutRequest(String paymentId) {
		return new OrderCreateRequest(
			null, "요청사항", null, OrderType.TAKEOUT, null,
			paymentId, false, null, MENU_PRICE, 0
		);
	}

	protected void givenPaidPayment(String paymentId) {
		given(portOneService.getPaidPayment(paymentId))
			.willReturn(TestFixture.createPortOnePaymentResponse(paymentId, "tx-" + paymentId, MENU_PRICE));
	}

	/**
	 * 주문 트랜잭션 안에서 호출되는 주문번호 생성 시점에, 커밋 직전에 실패하는 동기화를 등록
	 * 반환한 표식은 beforeCommit이 실제로 실행됐을 때만 true가 되어, 커밋 이전 실패와 구별할 수 있음
	 */
	protected AtomicBoolean failOrderTransactionAtCommit() {
		AtomicBoolean beforeCommitCalled = new AtomicBoolean(false);
		given(orderNumberUtil.createOrderNumber(anyLong(), any())).willAnswer(inv -> {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void beforeCommit(boolean readOnly) {
					beforeCommitCalled.set(true);
					throw new DataIntegrityViolationException("커밋 시점 제약 조건 위반");
				}
			});
			return "T-commit-fail";
		});
		return beforeCommitCalled;
	}

	/** 장바구니는 지연 로딩이라 트랜잭션 안에서 조회 */
	protected boolean hasCartItems(Users user) {
		return Boolean.TRUE.equals(transactionTemplate.execute(status ->
			!userRepository.findByUserNumber(user.getUserNumber()).orElseThrow().getCart().getCartItemList().isEmpty()));
	}
}