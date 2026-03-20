package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import jakarta.persistence.EntityNotFoundException;
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
		int countAddress = addressRepository.countByUser(user);
		if (countAddress > 0 && countAddress < 5) {
			addressRepository.findByUserAndIsPrimary(user, true)
				.ifPresent(existAddress -> {
					existAddress.updatePrimary(false);
					log.info("기존 기본 주소 비활성화 완료");
				});
		} else if (countAddress >= 5) {
			throw new IllegalArgumentException("주소는 5개까지 등록이 가능합니다.");
		}
		Addresses addresses = toEntity(addressRequestDto, user);

		addressRepository.save(addresses);
		log.info("주소 저장 성공");
	}

	@Override
	public void updateAddress(Long addressId, AddressRequestDto addressUpdateRequestDto) {
		Addresses addresses = addressRepository.findById(addressId)
			.orElseThrow(() -> new IllegalArgumentException("해당 주소 정보가 없습니다."));

		addresses.update(
			addressUpdateRequestDto.addressName(),
			addressUpdateRequestDto.roadName(),
			aesUtil.encryptToBase64(addressUpdateRequestDto.detailAddress()),
			addressUpdateRequestDto.postalCode(),
			addressUpdateRequestDto.category(),
			addressUpdateRequestDto.requestContent(),
			addressUpdateRequestDto.entrancePassword() != null ?
				aesUtil.encryptToBase64(addressUpdateRequestDto.entrancePassword()) : null,
			addressUpdateRequestDto.directionsInfo(),
			addressUpdateRequestDto.latitude(),
			addressUpdateRequestDto.longitude()
		);

		addressRepository.save(addresses);
	}

	@Override
	@Transactional
	public void updatePrimary(Long addressId, CustomOAuth2User customOAuth2User) {
		Addresses addresses = addressRepository.findById(addressId)
			.orElseThrow(() -> new IllegalArgumentException("해당 주소 정보가 없습니다."));

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
			.orElseThrow(() -> new EntityNotFoundException("조회된 기본 주소가 없습니다."));

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
	@Transactional
	public void deleteAddress(Long addressId, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		Addresses address = addressRepository.findById(addressId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 주소가 없습니다."));
		boolean isPrimary = address.getIsPrimary();

		addressRepository.delete(address);

		if (isPrimary) {
			log.info("기본 주소 삭제");
			addressRepository.findFirstByUserOrderByIdDesc(user)
				.ifPresentOrElse(
					latest -> {
						latest.updatePrimary(true);
						log.info("새 기본 주소 [{}]로 설정 완료", latest.getId());
					},
					() -> log.info("남은 주소 없음 — 기본 주소 설정 스킵")
				);
			log.info("가장 최근 등록 주소로 기본 주소 변경");
		}
	}

	@Override
	public void deleteOldAddress(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		Addresses address = addressRepository.findFirstByUserOrderByUpdateAtAsc(user)
			.orElseThrow(() -> new EntityNotFoundException("조회된 주소가 없습니다."));

		addressRepository.delete(address);
		log.info("가장 오래전 사용된 주소 삭제 성공");
	}

	private Addresses toEntity(AddressRequestDto addressRequestDto, Users user) {
		String addressName = hasText(addressRequestDto.addressName())
			? addressRequestDto.addressName()
			: addressRequestDto.roadName();

		String encryptedDetailAddress = aesUtil.encryptToBase64(addressRequestDto.detailAddress());

		String encryptedEntrancePassword = hasText(addressRequestDto.entrancePassword())
			? aesUtil.encryptToBase64(addressRequestDto.entrancePassword())
			: null;

		return Addresses.builder()
			.addressName(addressName)    //지정된 주소 이름이 없다면 도로명 주소로
			.roadName(addressRequestDto.roadName())
			.detailAddress(encryptedDetailAddress)
			.postalCode(addressRequestDto.postalCode())
			.latitude(addressRequestDto.latitude())
			.longitude(addressRequestDto.longitude())
			.lastUsedAt(LocalDateTime.now())
			.isPrimary(true)
			.category(addressRequestDto.category())
			.requestContent(addressRequestDto.requestContent())
			.entrancePassword(encryptedEntrancePassword)
			.directionsInfo(addressRequestDto.directionsInfo())
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
			.latitude(address.getLatitude())
			.longitude(address.getLongitude())
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

	private boolean hasText(String value) {
		return StringUtils.hasText(value);
	}
}
