package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
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
	@Transactional
	public void createAddress(AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User) {
		String userNumber = customOAuth2User.getName();
		Users user = userService.getUser(userNumber);

		if (addressRepository.existsByUser(user)) {
			addressRepository.findByUserAndIsPrimary(user, true)
				.ifPresent(existAddress -> {
					existAddress.updatePrimary(false);
					log.info("기존 기본 주소 비활성화 완료");
				});
		}
		Addresses addresses = toEntity(addressRequestDto, user);

		addressRepository.save(addresses);
		log.info("주소 저장 성공");
	}

	@Override
	public void updateAddress(Long addressId, AddressRequestDto addressUpdateRequestDto) {
		Addresses addresses = addressRepository.findById(addressId)
			.orElseThrow(() -> new NullPointerException("해당 주소 정보가 없습니다."));

		addresses.update(
			addressUpdateRequestDto.addressName(),
			addressUpdateRequestDto.roadName(),
			aesUtil.encryptToBase64(addressUpdateRequestDto.detailAddress()),
			addressUpdateRequestDto.postalCode(),
			addressUpdateRequestDto.category(),
			addressUpdateRequestDto.requestContent(),
			addressUpdateRequestDto.entrancePassword() != null ?
				aesUtil.encryptToBase64(addressUpdateRequestDto.entrancePassword()) : null,
			addressUpdateRequestDto.directionInfo()
		);

		addressRepository.save(addresses);
	}

	@Override
	@Transactional
	public void updatePrimary(Long addressId, CustomOAuth2User customOAuth2User) {
		Addresses addresses = addressRepository.findById(addressId)
			.orElseThrow(() -> new NullPointerException("해당 주소 정보가 없습니다."));

		String userNumber = customOAuth2User.getName();
		Users user = userService.getUser(userNumber);

		if (addressRepository.existsByUser(user)) {
			addressRepository.findByUserAndIsPrimary(user, true)
				.ifPresent(existAddress -> {
					existAddress.updatePrimary(false);
					log.info("기존 기본 주소 비활성화 완료");
				});
		}
		addresses.updatePrimary(true);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AddressResponseDto> getAllAddresses(CustomOAuth2User customOAuth2User) {
		String userNumber = customOAuth2User.getName();
		Users user = userService.getUser(userNumber);

		List<Addresses> addressList = addressRepository.findAllByUser(user);
		if (addressList.isEmpty()) {
			log.info("등록된 주소가 없음");
			return List.of();
		}
		log.info("주소 전체 목록 조회 완료");
		return toResponseDto(addressList);
	}

	@Override
	@Transactional(readOnly = true)
	public AddressResponseDto getAddress(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		Addresses address = addressRepository.findByUserAndIsPrimary(user, true)
			.orElseThrow(() -> new IllegalArgumentException("조회된 기본 주소가 없습니다."));

		return toResponseDto(address);
	}

	@Override
	@Transactional(readOnly = true)
	public AddressDetailResponseDto getAddressDetail(Long id, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		Addresses address = addressRepository.findByIdAndUser(id, user)
			.orElseThrow(() -> new IllegalArgumentException("조회된 주소가 없습니다."));
		return toDetailResponseDto(address);
	}

	@Override
	public void deleteAddress(Long addressId) {
		Addresses address = addressRepository.findById(addressId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 주소가 없습니다."));

		addressRepository.delete(address);
	}

	private Addresses toEntity(AddressRequestDto addressRequestDto, Users user) {
		return Addresses.builder()
			.addressName(addressRequestDto.addressName() != null ?
				addressRequestDto.addressName() : addressRequestDto.roadName())    //지정된 주소 이름이 없다면 도로명 주소로
			.roadName(addressRequestDto.roadName())
			.detailAddress(aesUtil.encryptToBase64(addressRequestDto.detailAddress()))
			.postalCode(addressRequestDto.postalCode())
			.isPrimary(true)
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

	//전체 목록 dto 변환
	private List<AddressResponseDto> toResponseDto(List<Addresses> addressesList) {
		return addressesList.stream()
			.map(addresses -> AddressResponseDto
				.builder()
				.id(addresses.getId())
				.addressName(addresses.getAddressName())
				.address(resolveAddressName(addresses.getRoadName(), addresses.getDetailAddress()))
				.isPrimary(addresses.getIsPrimary())
				.category(addresses.getCategory())
				.requestContent(addresses.getRequestContent())
				.entrancePassword(
					addresses.getEntrancePassword() != null ?
						aesUtil.decryptFromBase64(addresses.getEntrancePassword()) : null
				)
				.directionsInfo(addresses.getDirectionsInfo())
				.build())
			.collect(Collectors.toList());
	}

	// 단건 조회 dto 변환
	private AddressResponseDto toResponseDto(Addresses address) {
		return AddressResponseDto.builder()
			.id(address.getId())
			.addressName(address.getAddressName())
			.address(resolveAddressName(address.getRoadName(), address.getDetailAddress()))
			.isPrimary(address.getIsPrimary())
			.category(address.getCategory())
			.requestContent(address.getRequestContent())
			.entrancePassword(
				address.getEntrancePassword() != null ?
					aesUtil.decryptFromBase64(address.getEntrancePassword()) : null
			)
			.directionsInfo(address.getDirectionsInfo())
			.build();
	}

	//상세조회 dto 변환
	private AddressDetailResponseDto toDetailResponseDto(Addresses address) {
		return AddressDetailResponseDto.builder()
			.id(address.getId())
			.addressName(address.getAddressName())
			.roadName(address.getRoadName())
			.detailAddress(aesUtil.decryptFromBase64(address.getDetailAddress()))
			.postalCode(address.getPostalCode())
			.isPrimary(address.getIsPrimary())
			.category(address.getCategory())
			.requestContent(address.getRequestContent())
			.entrancePassword(
				address.getEntrancePassword() != null ?
					aesUtil.decryptFromBase64(address.getEntrancePassword()) : null
			)
			.directionsInfo(address.getDirectionsInfo())
			.build();
	}

	private String resolveAddressName(String roadName, String detailAddress) {
		return roadName + " " + aesUtil.decryptFromBase64(detailAddress);
	}
}
