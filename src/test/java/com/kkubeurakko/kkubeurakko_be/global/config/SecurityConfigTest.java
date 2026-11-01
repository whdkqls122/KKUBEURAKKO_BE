package com.kkubeurakko.kkubeurakko_be.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.auth.service.LogoutService;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAccessDeniedHandler;
import com.kkubeurakko.kkubeurakko_be.global.exception.GlobalAuthenticationEntryPoint;
import com.kkubeurakko.kkubeurakko_be.global.jwt.util.JwtUtil;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.oauth.handler.CustomSuccessHandler;
import com.kkubeurakko.kkubeurakko_be.global.oauth.service.CustomOAuth2UserService;

/**
 * SecurityConfig의 URL 인가 규칙만 검증
 * 실제 컨트롤러 대신 모든 요청에 200을 돌려주는 StubController를 띄워, 서비스 로직과 무관하게 권한 결과(401/403/200)만 확인
 */
@WebMvcTest(controllers = SecurityConfigTest.StubController.class)
@Import({
	SecurityConfig.class,
	GlobalAuthenticationEntryPoint.class,
	GlobalAccessDeniedHandler.class,
	SecurityConfigTest.StubController.class
})
class SecurityConfigTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	JwtUtil jwtUtil;
	@MockitoBean
	RedisTemplate<String, String> redisTemplate;
	@MockitoBean
	LogoutService logoutService;
	@MockitoBean
	CustomOAuth2UserService customOAuth2UserService;
	@MockitoBean
	CustomSuccessHandler customSuccessHandler;
	// oauth2Login 설정에 필요 (CI에는 application-local.yml이 없어 OAuth 등록 정보가 없음)
	@MockitoBean
	ClientRegistrationRepository clientRegistrationRepository;
	// 메인 클래스의 @EnableJpaAuditing 때문에 필요
	@MockitoBean
	JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@RestController
	static class StubController {
		@RequestMapping("/**")
		String ok() {
			return "ok";
		}
	}

	private static RequestPostProcessor loginAs(UserRole role) {
		CustomOAuth2User principal = new CustomOAuth2User(
			UserDto.builder().userNumber("test-user").role(role).build()
		);
		return authentication(
			new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
		);
	}

	@Nested
	@DisplayName("관리자 API")
	class AdminApi {

		@ParameterizedTest(name = "{0} {1}")
		@CsvSource({
			"GET, /v1/admin/statistics/daily",
			"GET, /v1/sales/monthly",
			"POST, /v1/menu/admin",
			"GET, /v1/menu/detail/admin/1",
			"DELETE, /v1/menu/admin/1",
			"POST, /v1/menu/option",
			"GET, /v1/menu/option-group",
			"POST, /v1/menu/menu-group",
			"PATCH, /v1/menu/menu-group/1/status",
			"GET, /v1/orders/1/orders",
			"PATCH, /v1/orders/1/accept",
			"PATCH, /v1/orders/1/refund",
			"PATCH, /v1/store/1/pause",
			"POST, /v1/store/1/holidays",
			"DELETE, /v1/store/1/holidays/clear",
			"POST, /v1/delivery/1",
			"GET, /v1/coupons/1",
			"POST, /v1/coupons/1",
			"PATCH, /v1/coupons/1/1/hide"
		})
		@DisplayName("비로그인은 401, 고객은 403, 관리자는 200")
		void adminOnly(String method, String uri) throws Exception {
			HttpMethod httpMethod = HttpMethod.valueOf(method);

			mockMvc.perform(request(httpMethod, uri))
				.andExpect(status().isUnauthorized());
			mockMvc.perform(request(httpMethod, uri).with(loginAs(UserRole.CUSTOMER)))
				.andExpect(status().isForbidden());
			mockMvc.perform(request(httpMethod, uri).with(loginAs(UserRole.ADMIN)))
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("고객 API")
	class CustomerApi {

		@ParameterizedTest(name = "{0} {1}")
		@CsvSource({
			"GET, /v1/coupons/issued",
			"GET, /v1/coupons",
			"POST, /v1/coupons/issued/1",
			"GET, /v1/orders",
			"POST, /v1/orders",
			"PATCH, /v1/account/password/me",
			"GET, /v1/store/1/status"
		})
		@DisplayName("비로그인은 401, 고객은 200")
		void loginRequired(String method, String uri) throws Exception {
			HttpMethod httpMethod = HttpMethod.valueOf(method);

			mockMvc.perform(request(httpMethod, uri))
				.andExpect(status().isUnauthorized());
			mockMvc.perform(request(httpMethod, uri).with(loginAs(UserRole.CUSTOMER)))
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("공개 API")
	class PublicApi {

		@ParameterizedTest(name = "{0} {1}")
		@CsvSource({
			"GET, /v1/menu/1",
			"GET, /v1/menu/detail/1",
			"GET, /v1/menu/menu-group",
			"POST, /v1/admin/login",
			"PATCH, /v1/account/password"
		})
		@DisplayName("비로그인도 200")
		void permitAll(String method, String uri) throws Exception {
			mockMvc.perform(request(HttpMethod.valueOf(method), uri))
				.andExpect(status().isOk());
		}
	}

	@Test
	@DisplayName("메뉴 수정은 GET 공개 규칙에 걸리지 않고 비로그인 시 401")
	void menuWriteIsNotPublic() throws Exception {
		mockMvc.perform(request(HttpMethod.PUT, "/v1/menu/admin/1"))
			.andExpect(status().isUnauthorized());
	}
}