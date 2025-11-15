package com.kkubeurakko.kkubeurakko_be.global.security.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.global.security.exception.AESDecryptException;
import com.kkubeurakko.kkubeurakko_be.global.security.exception.AESEncryptException;
import com.kkubeurakko.kkubeurakko_be.global.security.exception.InvalidAESKeyFormatException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AesUtil {
	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH_BIT = 128;
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	@Value("${spring.security.aes.secret}")
	private String secretKeyString;
	private SecretKeySpec keySpec;
	private final SecureRandom secureRandom = new SecureRandom();

	@PostConstruct
	private void init() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
			if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
				log.error("키 생성 중 오류 발생(잘못된 길이)");
				throw new IllegalArgumentException();
			}
			this.keySpec = new SecretKeySpec(keyBytes, "AES");
			log.info("키 생성 완료");
		} catch (IllegalArgumentException e) {
			throw new InvalidAESKeyFormatException(e.getMessage(), e);
		}
	}

	private String encryptToBase64(String plainText, byte[] optionalAad) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			secureRandom.nextBytes(iv);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

			if (optionalAad != null && optionalAad.length > 0) {
				cipher.updateAAD(optionalAad);
			}

			byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] out = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, out, 0, iv.length);
			System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(out);
		} catch (Exception e) {
			throw new AESEncryptException("AES암호화에 실패하였습니다.", e);
		}
	}

	private String decryptFromBase64(String base64IvAndCipherText, byte[] optionalAad) {
		try {
			byte[] all = Base64.getDecoder().decode(base64IvAndCipherText);
			if (all.length < IV_LENGTH + 1) {
				throw new IllegalArgumentException("잘못된 암호화 text");
			}

			byte[] iv = Arrays.copyOfRange(all, 0, IV_LENGTH);
			byte[] cipherText = Arrays.copyOfRange(all, IV_LENGTH, all.length);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

			if (optionalAad != null && optionalAad.length > 0) {
				cipher.updateAAD(optionalAad);
			}

			byte[] plain = cipher.doFinal(cipherText);
			return new String(plain, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("AES 복호화 중 오류 발생: {}", e.getMessage());
			throw new AESDecryptException("AES복호화에 실패 하였습니다", e);
		}
	}

	public String encryptToBase64(String plainText) {
		return encryptToBase64(plainText, null);
	}

	public String decryptFromBase64(String base64IvAndCipherText) {
		return decryptFromBase64(base64IvAndCipherText, null);
	}
}
