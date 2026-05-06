package com.kkubeurakko.kkubeurakko_be.domain.portone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.CashReceiptRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.IssueCashReceiptCustomerInput;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PaymentAmountInput;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneCashReceiptRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneErrorResponse;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentCancelRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOneVerifyInfoResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.ErrorAction;
import com.kkubeurakko.kkubeurakko_be.domain.portone.entity.PortOneErrorType;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentCancelException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOneVerifyException;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortOneServiceImpl implements PortOneService {
	private final RestTemplate restTemplate;
	@Value("${spring.portOne.store-id}")
	private String storeId;
	@Value("${spring.portOne.request-uri}")
	private String requestUri;
	@Value("${spring.portOne.secret-key}")
	private String secretKey;
	@Value("${spring.portOne.payment.channel-key}")
	private String paymentChannelKey;
	private final ObjectMapper objectMapper;

	@Override
	public PortOnePaymentResponseDto getPortOnePayment(String paymentId) {
		try {
			String url = requestUri + "/payments/" + paymentId;

			HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

			ResponseEntity<PortOnePaymentResponseDto> response =
				restTemplate.exchange(url, HttpMethod.GET, entity, PortOnePaymentResponseDto.class);

			return response.getBody();

		} catch (HttpStatusCodeException e) {
			log.error(
				"PortOne 결제 조회 실패 - status: {}, body: {}",
				e.getStatusCode(), e.getResponseBodyAsString()
			);
			throw new PortOnePaymentHistoryException("PortOne 결제 조회 실패: ", e);
		} catch (Exception e) {
			log.error("PortOne 결제 조회 중 예외", e);
			throw new PortOnePaymentHistoryException("PortOne 결제 조회 실패: ", e);
		}
	}

	@Override
	public PortOneVerifyInfoResponseDto getPortOneVerifyInfo(String identityVerificationId) {
		try {
			String url = requestUri + "/identity-verifications/" + identityVerificationId;

			HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

			ResponseEntity<PortOneVerifyInfoResponseDto> response =
				restTemplate.exchange(url, HttpMethod.GET, entity, PortOneVerifyInfoResponseDto.class);

			return response.getBody();
		} catch (HttpStatusCodeException e) {
			log.error(
				"PortOne 사용자 정보 조회 실패 - status: {}, body: {}",
				e.getStatusCode(), e.getResponseBodyAsString()
			);
			throw new PortOneVerifyException("PortOne 사용자 정보 조회 실패: 다시 요청 부탁드립니다.", e);
		} catch (Exception e) {
			log.error("PortOne 결제 조회 중 예외", e);
			throw new PortOneVerifyException("PortOne 사용자 정보 조회 실패: 다시 요청 부탁드립니다.", e);
		}
	}

	// 결제 정보 저장 실패 시 -> 자동 환불 로직
	@Override
	public void cancelPortOnePayment(String paymentId) {
		try {
			doCancelPayment(paymentId);
			log.info("결제 취소 성공 (paymentId={})", paymentId);

		} catch (HttpStatusCodeException e) {
			throw convertToException(e);
		} catch (Exception e) {
			log.error("결제 취소 실패", e);
			throw new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}

	@Override
	public void retryCancelPortOnePayment(String paymentId) {
		try {
			doCancelPayment(paymentId);
			log.info("결제 취소 재시도 성공 (paymentId={})", paymentId);

		} catch (HttpStatusCodeException e) {
			throw convertToException(e);
		} catch (Exception e) {
			log.error("결제 취소 재시도 실패", e);
			throw new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}

	@Override
	public void issueCashReceipt(Users user, String paymentId,
		CashReceiptRequestDto cashReceiptRequestDto, int totalAmount) {
		try {
			String url = requestUri + "/cash-receipts";

			HttpHeaders headers = createHeaders();

			PaymentAmountInput amount = PaymentAmountInput.builder()
				.total(totalAmount)
				.build();

			IssueCashReceiptCustomerInput customer = IssueCashReceiptCustomerInput.builder()
				.identityNumber(cashReceiptRequestDto.identityNumber())
				.name(user.getName())
				.email(user.getEmail())
				.build();

			PortOneCashReceiptRequestDto body = PortOneCashReceiptRequestDto.builder()
				.storeId(storeId)
				.paymentId(paymentId)
				.channelKey(paymentChannelKey)
				.type(cashReceiptRequestDto.type())
				.orderName("치킨 주문")
				.currency("KRW")
				.amount(amount)
				.customer(customer)
				.build();

			HttpEntity<PortOneCashReceiptRequestDto> entity =
				new HttpEntity<>(body, headers);

			restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

			log.info("현금영수증 발급 성공");
		} catch (HttpStatusCodeException e) {
			throw new PortOnePaymentCancelException(
				"현금영수증 발급 실패",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		} catch (Exception e) {
			log.error("현금영수증 발급 실패", e);
			throw new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}

	@Override
	public void cancelIssueCashReceipt(String paymentId) {
		try {
			doCancelCashReceipt(paymentId);
			log.info("현금영수증 취소 성공 (paymentId={})", paymentId);

		} catch (HttpStatusCodeException e) {
			log.error("현금영수증 취소 실패", e);
			throw new PortOnePaymentCancelException(
				"현금영수증 취소 실패",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		} catch (Exception e) {
			log.error("현금영수증 취소 실패", e);
			throw new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}

	@Override
	public void retryCancelIssueCashReceipt(String paymentId) {
		try {
			doCancelCashReceipt(paymentId);
			log.info("현금영수증 취소 재시도 성공 (paymentId={})", paymentId);

		} catch (HttpStatusCodeException e) {
			throw convertToException(e);
		} catch (Exception e) {
			log.error("현금영수증 취소 재시도 실패", e);
			throw new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}

	private void doCancelCashReceipt(String paymentId) {
		String url = requestUri + "/payments/" + paymentId + "/cash-receipt/cancel";

		HttpEntity<Void> entity =
			new HttpEntity<>(createHeaders());

		restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
	}

	private void doCancelPayment(String paymentId) {
		String url = requestUri + "/payments/" + paymentId + "/cancel";

		PortOnePaymentCancelRequestDto body =
			PortOnePaymentCancelRequestDto.builder()
				.reason("주문 생성 실패로 인한 자동 취소")
				.build();

		HttpEntity<PortOnePaymentCancelRequestDto> entity =
			new HttpEntity<>(body, createHeaders());

		restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "PortOne " + secretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private PortOnePaymentCancelException convertToException(HttpStatusCodeException e) {
		try {
			PortOneErrorResponse error =
				objectMapper.readValue(e.getResponseBodyAsString(), PortOneErrorResponse.class);

			PortOneErrorType type = PortOneErrorType.from(error.type());

			return new PortOnePaymentCancelException(
				error.message(),
				type.getAction(),
				type,
				e
			);
		} catch (Exception ex) {
			return new PortOnePaymentCancelException(
				"알 수 없는 오류",
				ErrorAction.RETRY,
				PortOneErrorType.UNKNOWN,
				e
			);
		}
	}
}
