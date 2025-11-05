package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
	private final UserService userService;
	private final AddressRepository addressRepository;

	@Override
	public void createAddress(AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User) {
		String userNumber = customOAuth2User.getName();
		Users user = userService.getUser(userNumber);

		Addresses addresses = toEntity(addressRequestDto, user);

		addressRepository.save(addresses);
	}

	private Addresses toEntity(AddressRequestDto addressRequestDto, Users user) {
		return Addresses.builder()
			.addressName(addressRequestDto.addressName())
			.roadName(addressRequestDto.roadName())
			.detailAddress(addressRequestDto.detailAddress())
			.postalCode(addressRequestDto.postalCode())
			.isPrimary(addressRequestDto.isPrimary())
			.category(addressRequestDto.category())
			.requestContent(addressRequestDto.requestContent())
			.entrancePassword(addressRequestDto.entrancePassword())
			.directionsInfo(addressRequestDto.directionInfo())
			.user(user)
			.build();
	}
}
