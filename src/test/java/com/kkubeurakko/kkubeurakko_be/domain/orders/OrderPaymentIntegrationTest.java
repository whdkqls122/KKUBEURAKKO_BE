package com.kkubeurakko.kkubeurakko_be.domain.orders;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.IllegalTransactionStateException;
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
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.repository.OrderRepository;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.OrderCreateFacadeService;
import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.PaymentAttemptStatus;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskType;
import com.kkubeurakko.kkubeurakko_be.global.task.repository.FailedTaskRepository;
import com.kkubeurakko.kkubeurakko_be.support.IntegrationTestSupport;

/**
 * 주문 생성·결제 처리 기록의 실제 DB 동작을 검증
 * PortOne(외부 API)과 주문번호 생성(영업시간 데이터 필요)만 IntegrationTestSupport의 mock을 쓰고 나머지는 실제 빈과 MySQL, Redis를 사용
 * 테스트끼리 데이터가 섞이지 않도록 결제 ID와 사용자를 테스트마다 새로 만든다.
 */
class OrderPaymentIntegrationTest extends IntegrationTestSupport {

	private static final int MENU_PRICE = 18000;
	// AdminInitializer가 앱 기동 시 만드는 가게
	private static final long STORE_ID = 1L;

	@Autowired
	private OrderCreateFacadeService orderCreateFacadeService;
	@Autowired
	private PaymentAttemptService paymentAttemptService;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private FailedTaskRepository failedTaskRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private MenuGroupRepository menuGroupRepository;
	@Autowired
	private MenuRepository menuRepository;
	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	// ===== 테스트 데이터 =====

	private String newPaymentId() {
		return "it-" + UUID.randomUUID().toString().substring(0, 12);
	}

	/** 메뉴 1개(18,000원)가 담긴 장바구니를 가진 사용자를 저장한다. */
	private Users saveUserWithCart() {
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
			.phone("it-phone-" + unique)
			.phoneHash("it-hash-" + unique)
			.nickname("통합테스트")
			.name("통합테스트")
			.role(UserRole.CUSTOMER)
			.cart(cart)
			.build());
	}

	private CustomOAuth2User loginAs(Users user) {
		CustomOAuth2User customOAuth2User = Mockito.mock(CustomOAuth2User.class);
		given(customOAuth2User.getName()).willReturn(user.getUserNumber());
		return customOAuth2User;
	}

	private OrderCreateRequest takeoutRequest(String paymentId) {
		return new OrderCreateRequest(
			null, "요청사항", null, OrderType.TAKEOUT, null,
			paymentId, false, null, MENU_PRICE, 0
		);
	}

	private void givenPaidPayment(String paymentId) {
		given(portOneService.getPaidPayment(paymentId))
			.willReturn(TestFixture.createPortOnePaymentResponse(paymentId, "tx-" + paymentId, MENU_PRICE));
	}

	private List<FailedTask> tasksOf(String paymentId) {
		return failedTaskRepository.findAll().stream()
			.filter(task -> task.getPayload().contains(paymentId))
			.toList();
	}

	private PaymentAttemptStatus attemptStatusOf(String paymentId) {
		return paymentAttemptService.findStatus(paymentId).orElseThrow();
	}

	// ===== 동시 요청 =====

	@Test
	@DisplayName("같은 결제로 두 요청이 겹치면 payment_attempt unique 제약으로 하나만 주문되고, 거절된 요청은 취소 작업을 만들거나 기록을 지우지 않는다")
	void concurrentSamePayment() throws Exception {
		// given
		Users user = saveUserWithCart();
		CustomOAuth2User customOAuth2User = loginAs(user);
		String paymentId = newPaymentId();
		OrderCreateRequest request = takeoutRequest(paymentId);

		// 첫 요청이 처리 기록을 커밋한 뒤 결제 조회에서 멈춰 있도록 해 두 요청을 겹치게 함
		CountDownLatch firstEntered = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		given(portOneService.getPaidPayment(paymentId)).willAnswer(inv -> {
			firstEntered.countDown();
			releaseFirst.await(10, TimeUnit.SECONDS);
			return TestFixture.createPortOnePaymentResponse(paymentId, "tx-" + paymentId, MENU_PRICE);
		});

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// when
			Future<Long> first = executor.submit(() -> orderCreateFacadeService.createOrder(request, customOAuth2User));
			assertThat(firstEntered.await(10, TimeUnit.SECONDS)).isTrue();

			assertThatThrownBy(() -> orderCreateFacadeService.createOrder(request, customOAuth2User))
				.isInstanceOf(InvalidPaymentException.class)
				.hasMessage("이미 처리 중이거나 사용된 결제입니다.");

			releaseFirst.countDown();
			Long orderId = first.get(10, TimeUnit.SECONDS);

			// then
			assertThat(orderId).isNotNull();
		} finally {
			executor.shutdownNow();
		}

		assertThat(orderRepository.findByUser(user)).hasSize(1);
		assertThat(paymentRepository.existsByPaymentId(paymentId)).isTrue();
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.COMPLETED);
		assertThat(tasksOf(paymentId)).isEmpty();
		verify(portOneService, times(1)).getPaidPayment(paymentId);
	}

	// ===== 취소 요청의 원자성 =====

	@Test
	@DisplayName("취소 요청 중 현금영수증 취소 작업 저장이 실패하면 상태 전이와 결제 취소 작업까지 모두 롤백되고, 다시 요청하면 함께 저장된다")
	void requestCancel_atomic() throws Exception {
		// given: 현금영수증을 요청한 결제의 처리 기록
		String paymentId = newPaymentId();
		paymentAttemptService.start(paymentId, true);
		willThrow(new IllegalStateException("현금영수증 취소 작업 저장 실패"))
			.given(failedTaskService).saveCashReceiptCancelTask(anyString(), anyString(), anyString());

		// when: 결제 취소 작업은 저장된 뒤 현금영수증 취소 작업 저장에서 실패
		assertThatThrownBy(() -> paymentAttemptService.requestCancel(paymentId, "IllegalStateException", "배달 불가"))
			.isInstanceOf(IllegalStateException.class);

		// then: 같은 트랜잭션이라 전부 롤백
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.PROCESSING);
		assertThat(tasksOf(paymentId)).isEmpty();

		// when: 저장 실패 원인이 사라진 뒤 다시 요청
		Mockito.reset(failedTaskService);
		boolean requested = paymentAttemptService.requestCancel(paymentId, "IllegalStateException", "배달 불가");

		// then: 상태와 두 작업이 함께 저장
		assertThat(requested).isTrue();
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.CANCEL_REQUESTED);
		assertThat(tasksOf(paymentId))
			.extracting(FailedTask::getTaskType)
			.containsExactlyInAnyOrder(TaskType.REFUND_PAYMENT, TaskType.CANCEL_CASH_RECEIPT);
	}

	// ===== 트랜잭션 전파 =====

	@Test
	@DisplayName("start(REQUIRES_NEW)는 호출한 트랜잭션이 롤백돼도 커밋된다")
	void start_requiresNew() {
		// given
		String paymentId = newPaymentId();

		// when
		transactionTemplate.executeWithoutResult(status -> {
			paymentAttemptService.start(paymentId, false);
			status.setRollbackOnly();
		});

		// then
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.PROCESSING);
	}

	@Test
	@DisplayName("complete(MANDATORY)는 트랜잭션 없이 호출하면 실패하고, 호출한 트랜잭션과 함께 커밋·롤백된다")
	void complete_mandatory() {
		// given
		String paymentId = newPaymentId();
		paymentAttemptService.start(paymentId, false);

		// 트랜잭션 없이 호출하면 실패
		assertThatThrownBy(() -> paymentAttemptService.complete(paymentId))
			.isInstanceOf(IllegalTransactionStateException.class);

		// 호출한 트랜잭션이 롤백되면 완료도 롤백
		transactionTemplate.executeWithoutResult(status -> {
			paymentAttemptService.complete(paymentId);
			status.setRollbackOnly();
		});
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.PROCESSING);

		// 호출한 트랜잭션이 커밋되면 완료도 커밋
		transactionTemplate.executeWithoutResult(status -> paymentAttemptService.complete(paymentId));
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.COMPLETED);
	}

	// ===== 커밋 시점 실패 =====

	@Test
	@DisplayName("주문 트랜잭션이 커밋 단계에서 실패하면 주문·결제·완료 상태는 롤백되고, Facade가 별도 트랜잭션으로 결제 취소를 요청한다")
	void commitFailed_requestCancel() {
		// given
		Users user = saveUserWithCart();
		CustomOAuth2User customOAuth2User = loginAs(user);
		String paymentId = newPaymentId();
		givenPaidPayment(paymentId);

		// 주문 트랜잭션 안에서 호출되는 주문번호 생성 시점에, 커밋 직전에 실패하는 동기화를 등록
		given(orderNumberUtil.createOrderNumber(anyLong(), any())).willAnswer(inv -> {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void beforeCommit(boolean readOnly) {
					throw new DataIntegrityViolationException("커밋 시점 제약 조건 위반");
				}
			});
			return "T-commit-fail";
		});

		// when & then
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(takeoutRequest(paymentId), customOAuth2User))
			.isInstanceOf(OrderCreateException.class)
			.hasMessageContaining("주문을 처리하는 중 오류가 발생했습니다.");

		// 주문 트랜잭션의 변경은 모두 롤백
		assertThat(orderRepository.findByUser(user)).isEmpty();
		assertThat(paymentRepository.existsByPaymentId(paymentId)).isFalse();
		Boolean cartRemains = transactionTemplate.execute(status ->
			!userRepository.findByUserNumber(user.getUserNumber()).orElseThrow().getCart().getCartItemList().isEmpty());
		assertThat(cartRemains).isTrue();

		// 취소 요청은 별도 트랜잭션으로 남음
		assertThat(attemptStatusOf(paymentId)).isEqualTo(PaymentAttemptStatus.CANCEL_REQUESTED);
		assertThat(tasksOf(paymentId))
			.extracting(FailedTask::getTaskType)
			.containsExactly(TaskType.REFUND_PAYMENT);
	}
}