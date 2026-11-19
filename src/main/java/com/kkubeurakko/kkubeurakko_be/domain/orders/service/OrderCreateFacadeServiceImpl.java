package com.kkubeurakko.kkubeurakko_be.domain.orders.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.orders.dto.OrderCreateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.orders.exception.OrderCreateException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.exception.InvalidPaymentException;
import com.kkubeurakko.kkubeurakko_be.domain.payments.repository.PaymentRepository;
import com.kkubeurakko.kkubeurakko_be.domain.payments.service.PaymentAttemptService;
import com.kkubeurakko.kkubeurakko_be.domain.portone.dto.PortOnePaymentResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.portone.exception.PortOnePaymentHistoryException;
import com.kkubeurakko.kkubeurakko_be.domain.portone.service.PortOneService;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 생성 흐름을 조율한다. 트랜잭션을 열지 않는다.
 * 외부 API(결제 조회·현금영수증 발급)는 트랜잭션 밖에서 호출하고, 주문 저장만 OrderCreateService의 트랜잭션에 맡긴다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreateFacadeServiceImpl implements OrderCreateFacadeService {
	private static final int MAX_REASON_LENGTH = 200;

	private final OrderCreateService orderCreateService;
	private final PaymentRepository paymentRepository;
	private final PaymentAttemptService paymentAttemptService;
	private final PortOneService portOneService;
	private final UserService userService;

	@Override
	public Long createOrder(OrderCreateRequest request, CustomOAuth2User customOAuth2User) {
		// 결제 확인 전 실패(중복, 처리 중, 조회 실패, 미완료 결제)는 결제 취소를 요청하지 않고 그대로 던짐
		PortOnePaymentResponseDto paymentResponse = getUsablePayment(request);

		try {
			// 결제된 금액으로 발급하고, 장바구니 금액과 다르면 주문 저장 단계에서 실패해 결제와 함께 취소된다
			issueCashReceiptIfRequested(request, customOAuth2User, paymentResponse.amount().total());

			// 서비스 메서드가 끝나며 커밋되므로, 커밋 시점 실패도 아래 catch에서 받는다
			return orderCreateService.createOrder(request, customOAuth2User, paymentResponse);
		} catch (Exception e) {
			// 여기부터는 우리 상점에서 완료된 결제이므로 환불 대상
			requestCancel(request.paymentId(), e);
			throw new OrderCreateException(
				toUserMessage(e) + "\n\n" +
					"결제는 잠시 후 자동으로 취소됩니다.\n" +
					"환불이 지연되거나 같은 문제가 지속될 경우 가게로 문의 부탁드립니다.",
				request.paymentId(),
				request.isCashReceipt(),
				e
			);
		}
	}

	/**
	 * 이미 사용된 결제인지 확인하고 결제 처리 기록을 남긴 뒤 결제 상태·상점을 확인한다.
	 * 실패 시 결제 취소를 요청하지 않는다.
	 */
	private PortOnePaymentResponseDto getUsablePayment(OrderCreateRequest request) {
		String paymentId = request.paymentId();
		if (paymentRepository.existsByPaymentId(paymentId)) {
			throw new InvalidPaymentException("이미 사용된 결제입니다.");
		}
		// 주문 처리 전에 기록을 먼저 커밋: 같은 결제의 동시 요청을 막고, 이후 주문이 완료되지 않으면 취소 대상으로 남긴다
		paymentAttemptService.start(paymentId, request.isCashReceipt());
		try {
			return portOneService.getPaidPayment(paymentId);
		} catch (InvalidPaymentException e) {
			// 완료되지 않았거나 우리 상점 결제가 아니면 취소할 결제가 없으므로, 결제 완료 후 다시 시도할 수 있게 기록을 지운다
			paymentAttemptService.discard(paymentId);
			throw e;
		} catch (PortOnePaymentHistoryException e) {
			// 결제 여부를 알 수 없으므로 기록을 남겨 방치 감지 스케줄러가 확인 후 취소하게 하고, 새 결제로 다시 주문하도록 안내한다
			throw new PortOnePaymentHistoryException(
				"결제 확인에 실패했습니다. 결제가 완료됐다면 잠시 후 자동으로 취소되니 다시 결제해 주세요.", e);
		}
	}

	private void issueCashReceiptIfRequested(OrderCreateRequest request, CustomOAuth2User customOAuth2User, int paidAmount) {
		if (!request.isCashReceipt()) {
			return;
		}
		Users user = userService.getUser(customOAuth2User.getName());
		portOneService.issueCashReceipt(user, request.paymentId(), request.cashReceiptRequest(), paidAmount);
	}

	/** 여기서 실패해도 결제 처리 기록이 처리 중으로 남아 방치 감지 스케줄러가 다시 취소를 요청한다. */
	private void requestCancel(String paymentId, Exception cause) {
		try {
			if (paymentAttemptService.requestCancel(paymentId, cause.getClass().getSimpleName(), toReason(cause))) {
				log.info("결제 취소 요청 (paymentId={}, 사유={})", paymentId, cause.getClass().getSimpleName());
			}
		} catch (Exception ex) {
			log.error("결제 취소 요청 실패, 방치 감지 스케줄러가 다시 처리 (paymentId={})", paymentId, ex);
		}
	}

	// 검증 실패처럼 사용자에게 보여줄 수 있는 메시지만 그대로 전달하고, DB 오류 등 내부 메시지는 숨긴다
	private String toUserMessage(Exception e) {
		if (e instanceof IllegalStateException
			|| e instanceof IllegalArgumentException
			|| e instanceof EntityNotFoundException) {
			return e.getMessage();
		}
		return "주문을 처리하는 중 오류가 발생했습니다.";
	}

	// 취소 작업의 실패 사유 컬럼에 들어가도록 길이를 제한한다
	private String toReason(Exception e) {
		String message = e.getMessage();
		if (message == null || message.length() <= MAX_REASON_LENGTH) {
			return message;
		}
		return message.substring(0, MAX_REASON_LENGTH);
	}
}