package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

import java.security.Principal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {

	private static final String TOKEN = "test-token";
	private static final String STORE_TOPIC = "/topic/store/1";

	@Mock
	JwtUtil jwtUtil;
	@Mock
	RedisTemplate<String, String> redisTemplate;
	@Mock
	MessageChannel channel;
	@InjectMocks
	StompAuthChannelInterceptor interceptor;

	/** 테스트용 STOMP 프레임 생성 */
	private Message<byte[]> stompMessage(StompCommand command, String authorization, String destination,
		Principal user) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
		if (authorization != null) {
			accessor.setNativeHeader("Authorization", authorization);
		}
		if (destination != null) {
			accessor.setDestination(destination);
		}
		if (user != null) {
			accessor.setUser(user);
		}
		// 인터셉터가 setUser로 헤더를 수정할 수 있도록 변경 가능한 상태로 둠
		accessor.setLeaveMutable(true);
		return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
	}

	private Authentication loginAs(UserRole role) {
		CustomOAuth2User principal = new CustomOAuth2User(
			UserDto.builder().userNumber("test-user").role(role).build()
		);
		return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
	}

	private void givenValidToken(String role) {
		given(redisTemplate.hasKey("blacklist:" + TOKEN)).willReturn(false);
		given(jwtUtil.isExpired(TOKEN)).willReturn(false);
		given(jwtUtil.getCategory(TOKEN)).willReturn("access");
		given(jwtUtil.getUserNumber(TOKEN)).willReturn("test-user");
		given(jwtUtil.getRole(TOKEN)).willReturn(role);
	}

	@Nested
	@DisplayName("CONNECT")
	class Connect {

		@Test
		@DisplayName("유효한 토큰이면 연결되고 세션에 사용자가 등록된다")
		void validToken() {
			givenValidToken("ADMIN");
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer " + TOKEN, null, null);

			Message<?> result = interceptor.preSend(message, channel);

			StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
			Authentication user = (Authentication)accessor.getUser();
			assertThat(user).isNotNull();
			assertThat(user.getAuthorities())
				.extracting("authority")
				.containsExactly("ADMIN");
		}

		@Test
		@DisplayName("Authorization 헤더가 없으면 거절한다")
		void noToken() {
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, null, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class);
		}

		@Test
		@DisplayName("Bearer 형식이 아니면 거절한다")
		void notBearer() {
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, TOKEN, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class);
		}

		@Test
		@DisplayName("로그아웃되어 블랙리스트에 있는 토큰은 거절한다")
		void blacklisted() {
			given(redisTemplate.hasKey("blacklist:" + TOKEN)).willReturn(true);
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer " + TOKEN, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class);
		}

		@Test
		@DisplayName("만료된 토큰은 거절한다")
		void expired() {
			given(redisTemplate.hasKey("blacklist:" + TOKEN)).willReturn(false);
			given(jwtUtil.isExpired(TOKEN)).willReturn(true);
			// 만료 검사 외에는 모두 정상인 토큰으로 만들어, 거절 이유가 만료 검사뿐이도록 함
			// (정상 코드에서는 만료 검사에서 끝나 호출되지 않으므로 lenient)
			lenient().when(jwtUtil.getCategory(TOKEN)).thenReturn("access");
			lenient().when(jwtUtil.getUserNumber(TOKEN)).thenReturn("test-user");
			lenient().when(jwtUtil.getRole(TOKEN)).thenReturn("ADMIN");
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer " + TOKEN, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class);
		}

		@Test
		@DisplayName("access 토큰이 아니면(refresh 토큰) 거절한다")
		void refreshToken() {
			given(redisTemplate.hasKey("blacklist:" + TOKEN)).willReturn(false);
			given(jwtUtil.isExpired(TOKEN)).willReturn(false);
			given(jwtUtil.getCategory(TOKEN)).willReturn("refresh");
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer " + TOKEN, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class);
		}

		@Test
		@DisplayName("토큰 파싱 중 예외가 나면 인증 실패로 변환해 거절한다")
		void parsingError() {
			given(redisTemplate.hasKey("blacklist:" + TOKEN)).willReturn(false);
			given(jwtUtil.isExpired(TOKEN)).willThrow(new IllegalArgumentException("invalid token"));
			Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer " + TOKEN, null, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(BadCredentialsException.class)
				.hasCauseInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("SUBSCRIBE")
	class Subscribe {

		@Test
		@DisplayName("관리자는 매장 주문 토픽을 구독할 수 있다")
		void admin() {
			Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, null, STORE_TOPIC, loginAs(UserRole.ADMIN));

			assertThat(interceptor.preSend(message, channel)).isSameAs(message);
		}

		@ParameterizedTest(name = "{0}")
		@ValueSource(strings = {"/topic/store/1", "/topic/**", "/topic/*/1", "/topic/notice"})
		@DisplayName("고객은 어떤 주소(패턴 포함)도 구독할 수 없다")
		void customer(String destination) {
			Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, null, destination,
				loginAs(UserRole.CUSTOMER));

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("인증 정보가 없으면 구독할 수 없다")
		void anonymous() {
			Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, null, STORE_TOPIC, null);

			assertThatThrownBy(() -> interceptor.preSend(message, channel))
				.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Test
	@DisplayName("클라이언트의 SEND는 관리자라도 거절한다")
	void send() {
		Message<byte[]> message = stompMessage(StompCommand.SEND, null, "/topic/store/1", loginAs(UserRole.ADMIN));

		assertThatThrownBy(() -> interceptor.preSend(message, channel))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("DISCONNECT 등 그 외 명령은 그대로 통과시킨다")
	void otherCommand() {
		Message<byte[]> message = stompMessage(StompCommand.DISCONNECT, null, null, null);

		assertThat(interceptor.preSend(message, channel)).isSameAs(message);
	}
}