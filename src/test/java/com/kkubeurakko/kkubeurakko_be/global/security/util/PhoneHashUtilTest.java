package com.kkubeurakko.kkubeurakko_be.global.security.util;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PhoneHashUtilTest {

	private static final String KEY = base64("test-phone-hash-key-32-bytes-ok!");
	private static final String OTHER_KEY = base64("other-phone-hash-key-32-bytes!!!");
	private static final String PHONE = "01012345678";

	private final PhoneHashUtil phoneHashUtil = new PhoneHashUtil(KEY);

	private static String base64(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("같은 번호는 항상 같은 해시가 나온다")
	void sameInput_sameHash() {
		assertThat(phoneHashUtil.hash(PHONE)).isEqualTo(phoneHashUtil.hash(PHONE));
	}

	@Test
	@DisplayName("키가 다르면 같은 번호라도 다른 해시가 나온다")
	void differentKey_differentHash() {
		PhoneHashUtil otherKeyUtil = new PhoneHashUtil(OTHER_KEY);

		assertThat(otherKeyUtil.hash(PHONE)).isNotEqualTo(phoneHashUtil.hash(PHONE));
	}

	@Test
	@DisplayName("하이픈·공백 표기와 무관하게 같은 번호는 같은 해시가 나온다")
	void normalized() {
		assertThat(phoneHashUtil.hash("010-1234-5678")).isEqualTo(phoneHashUtil.hash(PHONE));
		assertThat(phoneHashUtil.hash(" 010 1234 5678 ")).isEqualTo(phoneHashUtil.hash(PHONE));
	}

	@Test
	@DisplayName("키 없이 계산할 수 있는 SHA-256 해시와 다르다")
	void notPlainSha256() throws Exception {
		byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(PHONE.getBytes(StandardCharsets.UTF_8));

		assertThat(phoneHashUtil.hash(PHONE)).isNotEqualTo(Base64.getEncoder().encodeToString(sha256));
	}

	@Test
	@DisplayName("숫자가 없는 번호는 거절한다")
	void invalidPhone_rejected() {
		assertThatThrownBy(() -> phoneHashUtil.hash("---"))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> phoneHashUtil.hash(null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("32바이트보다 짧은 키는 거절한다")
	void shortKey_rejected() {
		assertThatThrownBy(() -> new PhoneHashUtil(base64("short-key")))
			.isInstanceOf(IllegalStateException.class);
	}
}