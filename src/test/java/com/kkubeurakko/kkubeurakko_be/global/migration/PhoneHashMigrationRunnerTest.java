package com.kkubeurakko.kkubeurakko_be.global.migration;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;
import com.kkubeurakko.kkubeurakko_be.global.security.util.PhoneHashUtil;
import com.kkubeurakko.kkubeurakko_be.support.IntegrationTestSupport;

/**
 * 기존 SHA-256 해시로 저장된 사용자가 마이그레이션 후 새 HMAC 해시로 조회되는지 검증
 * 러너 빈은 설정으로 꺼져 있어, 컨텍스트를 바꾸지 않도록 같은 의존성으로 직접 만들어 실행
 */
class PhoneHashMigrationRunnerIntegrationTest extends IntegrationTestSupport {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AesUtil aesUtil;
	@Autowired
	private PhoneHashUtil phoneHashUtil;
	@Autowired
	private PlatformTransactionManager transactionManager;

	private String legacySha256(String phone) throws Exception {
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(phone.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(digest);
	}

	private void runMigration() {
		PhoneHashMigrationRunner runner = new PhoneHashMigrationRunner(userRepository, aesUtil, phoneHashUtil);
		new TransactionTemplate(transactionManager).executeWithoutResult(status -> runner.run(null));
	}

	@Test
	@DisplayName("기존 SHA-256 해시 사용자가 마이그레이션 후 새 해시로 조회되고, 다시 실행해도 결과가 같다")
	void migrate_legacyHash() throws Exception {
		// given: 이전 방식으로 저장된 사용자
		String phone = String.format("010%08d", ThreadLocalRandom.current().nextInt(100_000_000));
		String unique = UUID.randomUUID().toString().substring(0, 8);
		userRepository.save(Users.builder()
			.userNumber("it-user-" + unique)
			.phone(aesUtil.encryptToBase64(phone))
			.phoneHash(legacySha256(phone))
			.nickname("통합테스트")
			.name("통합테스트")
			.role(UserRole.CUSTOMER)
			.build());

		// when
		runMigration();

		// then
		String newHash = phoneHashUtil.hash(phone);
		assertThat(userRepository.findByPhoneHash(newHash)).isPresent();
		assertThat(userRepository.findByPhoneHash(legacySha256(phone))).isEmpty();

		// 다시 실행해도 같은 해시 유지
		runMigration();
		assertThat(userRepository.findByPhoneHash(newHash)).isPresent();
	}
}