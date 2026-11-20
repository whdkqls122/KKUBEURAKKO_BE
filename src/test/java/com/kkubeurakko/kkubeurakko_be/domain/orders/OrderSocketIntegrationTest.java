package com.kkubeurakko.kkubeurakko_be.domain.orders;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.TokenService;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.orders.service.OrderCreateFacadeService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.support.OrderIntegrationTestSupport;

/**
 * 실제 서버에 STOMP로 접속해 웹소켓 인증·구독 제한과 주문 커밋 후 알림을 검증
 */
class OrderSocketIntegrationTest extends OrderIntegrationTestSupport {

	private static final String STORE_TOPIC = "/topic/store/" + STORE_ID;
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	@LocalServerPort
	private int port;

	@Autowired
	private OrderCreateFacadeService orderCreateFacadeService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Value("${spring.jwt.admin-access-token-expiry}")
	private long accessTokenExpiry;

	private WebSocketStompClient stompClient;
	private final List<StompSession> sessions = new ArrayList<>();

	@BeforeEach
	void setUpStompClient() {
		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@AfterEach
	void disconnect() {
		sessions.stream().filter(StompSession::isConnected).forEach(StompSession::disconnect);
		sessions.clear();
	}

	// ===== 접속 도우미 =====

	private String accessToken(String role) {
		return jwtUtil.createAccessToken("it-" + UUID.randomUUID().toString().substring(0, 8), role, accessTokenExpiry);
	}

	private CompletableFuture<StompSession> connectAsync(String accessToken) {
		StompHeaders connectHeaders = new StompHeaders();
		if (accessToken != null) {
			connectHeaders.add("Authorization", "Bearer " + accessToken);
		}
		return stompClient.connectAsync(
			"ws://localhost:" + port + "/ws",
			new WebSocketHttpHeaders(),
			connectHeaders,
			new StompSessionHandlerAdapter() {
			}
		);
	}

	private StompSession connect(String accessToken) throws Exception {
		StompSession session = connectAsync(accessToken).get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		sessions.add(session);
		return session;
	}

	private List<Map<String, Object>> subscribe(StompSession session) {
		List<Map<String, Object>> received = new CopyOnWriteArrayList<>();
		session.subscribe(STORE_TOPIC, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Map.class;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void handleFrame(StompHeaders headers, Object payload) {
				received.add((Map<String, Object>)payload);
			}
		});
		return received;
	}

	/** 구독은 비동기로 등록되므로, 확인용 메시지가 도착할 때까지 보내 본 뒤 받은 목록을 비움 */
	private List<Map<String, Object>> subscribeAndAwaitReady(StompSession session) {
		List<Map<String, Object>> received = subscribe(session);
		await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(200)).until(() -> {
			messagingTemplate.convertAndSend(STORE_TOPIC, Map.of("probe", true));
			return received.stream().anyMatch(message -> message.containsKey("probe"));
		});
		received.clear();
		return received;
	}

	private Optional<Map<String, Object>> orderMessageOf(List<Map<String, Object>> received, String orderNumber) {
		return received.stream()
			.filter(message -> orderNumber.equals(message.get("orderNumber")))
			.findFirst();
	}

	// ===== 주문 알림 =====

	@Test
	@DisplayName("관리자가 가게 토픽을 구독하면 주문이 커밋된 뒤 복호화된 전화번호와 함께 주문 알림을 받는다")
	void admin_receivesOrderAfterCommit() throws Exception {
		// given
		String phone = newPhone();
		Users user = saveUserWithCart(phone);
		String paymentId = newPaymentId();
		givenPaidPayment(paymentId);
		String orderNumber = "T-" + paymentId;
		given(orderNumberUtil.createOrderNumber(anyLong(), any())).willReturn(orderNumber);

		List<Map<String, Object>> received = subscribeAndAwaitReady(connect(accessToken("ADMIN")));

		// when
		Long orderId = orderCreateFacadeService.createOrder(takeoutRequest(paymentId), loginAs(user));

		// then
		await().atMost(TIMEOUT).until(() -> orderMessageOf(received, orderNumber).isPresent());
		Map<String, Object> message = orderMessageOf(received, orderNumber).orElseThrow();
		assertThat(((Number)message.get("orderId")).longValue()).isEqualTo(orderId);
		assertThat(message)
			.containsEntry("orderType", "TAKEOUT")
			.containsEntry("phone", phone);
	}

	@Test
	@DisplayName("주문 트랜잭션이 커밋 단계에서 실패하면 주문 알림을 보내지 않는다")
	void commitFailed_noOrderMessage() throws Exception {
		// given
		Users user = saveUserWithCart();
		String paymentId = newPaymentId();
		givenPaidPayment(paymentId);
		failOrderTransactionAtCommit();

		List<Map<String, Object>> received = subscribeAndAwaitReady(connect(accessToken("ADMIN")));

		// when
		assertThatThrownBy(() -> orderCreateFacadeService.createOrder(takeoutRequest(paymentId), loginAs(user)))
			.isInstanceOf(OrderCreateException.class);

		// then: 주문 생성 이벤트는 발행됐지만 커밋되지 않아 AFTER_COMMIT 리스너가 실행되지 않음
		await().during(Duration.ofSeconds(1)).atMost(TIMEOUT)
			.until(() -> orderMessageOf(received, "T-commit-fail").isEmpty());
	}

	// ===== 인증·구독 제한 =====

	@Test
	@DisplayName("토큰 없이 접속하면 CONNECT가 거부된다")
	void connect_withoutToken_rejected() {
		assertThatThrownBy(() -> connectAsync(null).get(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
			.isInstanceOf(ExecutionException.class);
	}

	@Test
	@DisplayName("로그아웃으로 블랙리스트에 등록된 토큰으로 접속하면 CONNECT가 거부된다")
	void connect_withBlacklistedToken_rejected() {
		// given
		String accessToken = accessToken("ADMIN");
		tokenService.blacklistAccessToken(accessToken);

		// when & then
		assertThatThrownBy(() -> connectAsync(accessToken).get(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
			.isInstanceOf(ExecutionException.class);
	}

	@Test
	@DisplayName("고객 토큰은 접속할 수 있지만 가게 토픽을 구독하면 서버가 연결을 끊는다")
	void customer_subscribe_rejected() throws Exception {
		// given
		StompSession session = connect(accessToken("CUSTOMER"));
		assertThat(session.isConnected()).isTrue();

		// when
		subscribe(session);

		// then
		await().atMost(TIMEOUT).until(() -> !session.isConnected());
	}
}