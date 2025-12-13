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

import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentCancelRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentCancelException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortOneServiceImpl implements PortOneService {
	private final RestTemplate restTemplate;
	@Value("${spring.portone.store-id}")
	private String storeId;
	@Value("${spring.portone.request-uri}")
	private String requestUri;
	@Value("${spring.portone.secret-key}")
	private String secretKey;

	@Override
	public PortOnePaymentResponseDto getPortOnePayment(String paymentId) {
		try {
			String url = requestUri + "/payments/" + paymentId;

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "PortOne " + secretKey);
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Void> entity = new HttpEntity<>(headers);

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

	// 결제 정보 저장 실패 시 -> 자동 환불 로직
	@Override
	public void cancelPortOnePayment(String paymentId) {

		String url = requestUri + "/payments/" + paymentId + "/cancel";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "PortOne " + secretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		PortOnePaymentCancelRequestDto body = PortOnePaymentCancelRequestDto.builder()
			.reason("주문 생성 중 오류로 인한 자동 취소")
			.build();

		HttpEntity<PortOnePaymentCancelRequestDto> entity = new HttpEntity<>(body, headers);
		try {
			restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
			log.info("결제 취소 성공 (paymentId={})", paymentId);
		} catch (HttpStatusCodeException e) {
			log.error(
				"PortOne 결제 취소 실패 - status: {}, body: {}",
				e.getStatusCode(),
				e.getResponseBodyAsString()
			);
			throw new PortOnePaymentCancelException("결제 취소 실패", e);
		}
	}
}
