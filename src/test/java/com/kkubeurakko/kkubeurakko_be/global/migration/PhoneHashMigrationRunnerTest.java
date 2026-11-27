package com.kkubeurakko.kkubeurakko_be.global.migration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.UserRepository;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;
import com.kkubeurakko.kkubeurakko_be.global.security.util.PhoneHashUtil;

@ExtendWith(MockitoExtension.class)
class PhoneHashMigrationRunnerTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private AesUtil aesUtil;
	@Mock
	private PhoneHashUtil phoneHashUtil;

	@InjectMocks
	private PhoneHashMigrationRunner runner;

	private Users user(String userNumber, String encryptedPhone, String phoneHash) {
		return Users.builder()
			.userNumber(userNumber)
			.phone(encryptedPhone)
			.phoneHash(phoneHash)
			.nickname("닉네임")
			.name("이름")
			.role(UserRole.CUSTOMER)
			.build();
	}

	@Test
	@DisplayName("이전 해시 사용자만 갱신하고, 이미 새 해시인 사용자는 건너뛴다")
	void migrate_skipsAlreadyMigrated() {
		// given
		Users legacy = user("kakao_1", "enc-1", "legacy-hash");
		Users migrated = user("kakao_2", "enc-2", "new-hash-2");
		when(userRepository.findAll()).thenReturn(List.of(legacy, migrated));
		when(aesUtil.decryptFromBase64("enc-1")).thenReturn("01011112222");
		when(aesUtil.decryptFromBase64("enc-2")).thenReturn("01033334444");
		when(phoneHashUtil.hash("01011112222")).thenReturn("new-hash-1");
		when(phoneHashUtil.hash("01033334444")).thenReturn("new-hash-2");

		// when
		int updated = runner.migrate();

		// then
		assertThat(updated).isEqualTo(1);
		assertThat(legacy.getPhoneHash()).isEqualTo("new-hash-1");
		assertThat(migrated.getPhoneHash()).isEqualTo("new-hash-2");
	}

	@Test
	@DisplayName("복호화에 실패하면 실패한 사용자 번호를 담은 예외로 중단한다")
	void migrate_failure_includesUserNumber() {
		// given
		Users broken = user("kakao_broken", "broken-cipher", "legacy-hash");
		IllegalArgumentException cause = new IllegalArgumentException("복호화 실패");
		when(userRepository.findAll()).thenReturn(List.of(broken));
		when(aesUtil.decryptFromBase64("broken-cipher")).thenThrow(cause);

		// when & then
		assertThatThrownBy(() -> runner.migrate())
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("userNumber=kakao_broken")
			.hasCause(cause);
		assertThat(broken.getPhoneHash()).isEqualTo("legacy-hash");
	}
}