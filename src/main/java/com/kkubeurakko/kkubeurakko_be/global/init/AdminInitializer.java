package com.kkubeurakko.kkubeurakko_be.global.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.domain.stores.repository.StoreRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Admins;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.repository.AdminRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final StoreRepository storeRepository;
	@Value("${admin.initial.id}")
	private String id;
	@Value("${admin.initial.password}")
	private String password;

	@PostConstruct
	public void init() {
		Stores store = initStore();
		initAdmin(store);
	}

	private Stores initStore() {
		if (!storeRepository.existsById(1L)) {
			Stores store = Stores.builder()
				.status(StoreStatus.OPEN)
				.minOrderPrice(17000)
				.packagingAvgTime(10)
				.deliveryAvgTime(60)
				.build();

			return storeRepository.save(store);
		}
		return null;
	}

	private void initAdmin(Stores store) {
		if (!adminRepository.existsById(id)) {
			Admins admin = Admins.builder()
				.id(id)
				.password(passwordEncoder.encode(password))
				.role(UserRole.ADMIN)
				.store(store)
				.build();

			adminRepository.save(admin);
		}
	}
}
