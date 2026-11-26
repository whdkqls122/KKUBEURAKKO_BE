package com.kkubeurakko.kkubeurakko_be.global.migration;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;
import com.kkubeurakko.kkubeurakko_be.global.security.util.PhoneHashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기존 SHA-256 전화번호 해시를 HMAC-SHA256으로 다시 계산하는 일회성 마이그레이션
 * migration.phone-hash.enabled=true로 한 번 실행한 뒤 설정을 끄고, 모든 환경에 반영되면 이 클래스를 삭제
 * 이미 새 해시인 행은 건너뛰어 여러 번 실행해도 결과가 같음
 */
@Component
@ConditionalOnProperty(name = "migration.phone-hash.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PhoneHashMigrationRunner implements ApplicationRunner {

	private final UserRepository userRepository;
	private final AesUtil aesUtil;
	private final PhoneHashUtil phoneHashUtil;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<Users> users = userRepository.findAll();
		int updated = 0;

		for (Users user : users) {
			String phone = aesUtil.decryptFromBase64(user.getPhone());
			String newHash = phoneHashUtil.hash(phone);
			if (!newHash.equals(user.getPhoneHash())) {
				user.updatePhoneHash(newHash);
				updated++;
			}
		}
		log.info("전화번호 해시 재계산 완료 (전체={}, 갱신={})", users.size(), updated);
	}
}