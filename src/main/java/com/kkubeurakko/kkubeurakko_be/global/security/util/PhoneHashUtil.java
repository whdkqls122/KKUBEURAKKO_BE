package com.kkubeurakko.kkubeurakko_be.global.security.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.users.exception.PhoneHashingException;

/**
 * 전화번호로 사용자를 찾기 위한 해시
 * 같은 번호는 같은 값이 나와야 조회할 수 있어 사용자별 솔트 대신 서버 비밀 키로 계산하는 HMAC-SHA256을 사용
 * DB만 유출돼서는 키가 없어 번호를 전수 대입으로 복원할 수 없음
 */
@Component
public class PhoneHashUtil {

	private static final String ALGORITHM = "HmacSHA256";
	// HMAC-SHA256 출력 길이와 같은 32바이트 이상을 요구
	private static final int MIN_KEY_BYTES = 32;

	private final SecretKeySpec secretKey;

	public PhoneHashUtil(@Value("${spring.security.phone-hash.secret}") String base64Secret) {
		byte[] keyBytes = decodeKey(base64Secret);
		if (keyBytes.length < MIN_KEY_BYTES) {
			throw new IllegalStateException("전화번호 해시 키는 " + MIN_KEY_BYTES + "바이트 이상이어야 합니다.");
		}
		this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
	}

	public String hash(String phone) {
		String normalized = normalize(phone);
		try {
			// Mac은 스레드 안전하지 않아 호출마다 새로 만듦
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(secretKey);
			byte[] digest = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(digest);
		} catch (GeneralSecurityException e) {
			throw new PhoneHashingException("전화번호 해시 생성에 실패 하였습니다.", e);
		}
	}

	/** 하이픈 및 공백 등 표기 차이로 같은 번호가 다른 해시가 되지 않도록 숫자만 남김 */
	private String normalize(String phone) {
		if (phone == null) {
			throw new IllegalArgumentException("전화번호가 없습니다.");
		}
		String digits = phone.replaceAll("[^0-9]", "");
		if (digits.isEmpty()) {
			throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
		}
		return digits;
	}

	private byte[] decodeKey(String base64Secret) {
		try {
			return Base64.getDecoder().decode(base64Secret);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("전화번호 해시 키는 Base64 형식이어야 합니다.", e);
		}
	}
}