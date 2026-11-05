package com.kkubeurakko.kkubeurakko_be.domain.portone.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;

@ExtendWith(MockitoExtension.class)
class PortOneServiceImplTest {
	private static final String STORE_ID = "store-test-001";
	private static final String PAYMENT_ID = "payment-1";

	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ObjectMapper objectMapper;
	@InjectMocks
	private PortOneServiceImpl portOneService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(portOneService, "storeId", STORE_ID);
		ReflectionTestUtils.setField(portOneService, "requestUri", "https://api.portone.test");
		ReflectionTestUtils.setField(portOneService, "secretKey", "test-secret");
	}

	private void givenPortOneResponse(PortOnePaymentResponseDto response) {
		given(restTemplate.exchange(
			anyString(),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			eq(PortOnePaymentResponseDto.class)
		)).willReturn(ResponseEntity.ok(response));
	}

	/** TestFixture 응답에서 상태·상점만 바꾼 결제 응답 */
	private PortOnePaymentResponseDto payment(String status, String storeId, int total) {
		PortOnePaymentResponseDto base = TestFixture.createPortOnePaymentResponse(PAYMENT_ID, "tx-1", total);
		return new PortOnePaymentResponseDto(status, base.id(), base.transactionId(), storeId, base.amount(),
			base.method());
	}

	@Test
	@DisplayName("완료 상태이고 상점·금액이 일치하면 결제 정보를 반환한다")
	void getVerifiedPayment_success() {
		givenPortOneResponse(payment("PAID", STORE_ID, 36000));

		PortOnePaymentResponseDto result = portOneService.getVerifiedPayment(PAYMENT_ID, 36000);

		assertThat(result.status()).isEqualTo("PAID");
	}

	@Test
	@DisplayName("결제가 완료 상태가 아니면 InvalidPaymentException이 발생한다")
	void getVerifiedPayment_notPaid() {
		givenPortOneResponse(payment("READY", STORE_ID, 36000));

		assertThatThrownBy(() -> portOneService.getVerifiedPayment(PAYMENT_ID, 36000))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("결제가 완료되지 않았습니다.");
	}

	@Test
	@DisplayName("다른 상점의 결제면 InvalidPaymentException이 발생한다")
	void getVerifiedPayment_otherStore() {
		givenPortOneResponse(payment("PAID", "store-other", 36000));

		assertThatThrownBy(() -> portOneService.getVerifiedPayment(PAYMENT_ID, 36000))
			.isInstanceOf(InvalidPaymentException.class)
			.hasMessage("결제 상점 정보가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("결제 금액이 다르면 환불 대상 예외(IllegalStateException)가 발생한다")
	void getVerifiedPayment_amountMismatch() {
		givenPortOneResponse(payment("PAID", STORE_ID, 30000));

		assertThatThrownBy(() -> portOneService.getVerifiedPayment(PAYMENT_ID, 36000))
			.isInstanceOf(IllegalStateException.class)
			.isNotInstanceOf(InvalidPaymentException.class)
			.hasMessage("결제 금액이 일치하지 않습니다.");
	}
}