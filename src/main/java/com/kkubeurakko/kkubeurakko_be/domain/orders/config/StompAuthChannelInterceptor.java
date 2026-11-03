package com.kkubeurakko.kkubeurakko_be.domain.orders.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";
	private static final String STORE_TOPIC_PREFIX = "/topic/store/";

	private final JwtUtil jwtUtil;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		switch (accessor.getCommand()) {
			case CONNECT -> accessor.setUser(authenticate(accessor));
			case SUBSCRIBE -> checkSubscribe(accessor);
			case SEND -> throw new AccessDeniedException("클라이언트 메시지 전송은 허용되지 않습니다.");
			default -> {
			}
		}
		return message;
	}

	private Authentication authenticate(StompHeaderAccessor accessor) {
		String header = accessor.getFirstNativeHeader(AUTHORIZATION);
		if (header == null || !header.startsWith(BEARER)) {
			throw new BadCredentialsException("토큰이 없습니다.");
		}
		String token = header.substring(BEARER.length());

		try {
			if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))
				|| jwtUtil.isExpired(token)
				|| !"access".equals(jwtUtil.getCategory(token))) {
				throw new BadCredentialsException("유효하지 않은 토큰입니다.");
			}

			CustomOAuth2User principal = new CustomOAuth2User(
				UserDto.builder()
					.userNumber(jwtUtil.getUserNumber(token))
					.role(UserRole.findUserRole(jwtUtil.getRole(token)))
					.build()
			);
			return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		} catch (BadCredentialsException e) {
			throw e;
		} catch (Exception e) {
			// 만료
			throw new BadCredentialsException("유효하지 않은 토큰입니다.", e);
		}
	}

	private void checkSubscribe(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith(STORE_TOPIC_PREFIX)) {
			return;
		}

		Authentication user = (Authentication)accessor.getUser();
		boolean isAdmin = user != null && user.getAuthorities().stream()
			.anyMatch(authority -> UserRole.ADMIN.name().equals(authority.getAuthority()));
		if (!isAdmin) {
			throw new AccessDeniedException("매장 주문 알림은 관리자만 구독할 수 있습니다.");
		}
	}
}