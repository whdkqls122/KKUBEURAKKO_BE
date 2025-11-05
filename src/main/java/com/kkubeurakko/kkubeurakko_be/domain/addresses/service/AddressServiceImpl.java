package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import org.springframework.stereotype.Service;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
	private final UserService userService;
	private final AddressRepository addressRepository;
	private final AesUtil aesUtil;

	@Override
	public void createAddress(AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User) {
		String userNumber = customOAuth2User.getName();
		Users user = userService.getUser(userNumber);

		Addresses addresses = toEntity(addressRequestDto, user);

		addressRepository.save(addresses);
		log.info("주소 저장 성공");
	}

	@Override
	public void updateAddress(Long id, AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User) {
		Addresses addresses = addressRepository.findById(id)
			.orElseThrow(() -> new NullPointerException("해당 주소 정보가 없습니다."));

		addresses.update(
			addressRequestDto.addressName(),
			addressRequestDto.roadName(),
			aesUtil.encryptToBase64(addressRequestDto.detailAddress()),
			aesUtil.encryptToBase64(addressRequestDto.postalCode()),
			addressRequestDto.isPrimary(),
			addressRequestDto.category(),
			addressRequestDto.requestContent(),
			addressRequestDto.entrancePassword() != null ?
				aesUtil.encryptToBase64(addressRequestDto.entrancePassword()) : null,
			addressRequestDto.directionInfo()
		);

		addressRepository.save(addresses);
	}

	private Addresses toEntity(AddressRequestDto addressRequestDto, Users user) {
		return Addresses.builder()
			.addressName(addressRequestDto.addressName())
			.roadName(addressRequestDto.roadName())
			.detailAddress(aesUtil.encryptToBase64(addressRequestDto.detailAddress()))
			.postalCode(aesUtil.encryptToBase64(addressRequestDto.postalCode()))
			.isPrimary(addressRequestDto.isPrimary())
			.category(addressRequestDto.category())
			.requestContent(addressRequestDto.requestContent())
			.entrancePassword(
				addressRequestDto.entrancePassword() != null ?
					aesUtil.encryptToBase64(addressRequestDto.entrancePassword()) : null
			)
			.directionsInfo(addressRequestDto.directionInfo())
			.user(user)
			.build();
	}
}
