package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {
	@Mock
	private AddressRepository addressRepository;

	@Mock
	private UserService userService;
	@Mock
	private AesUtil aesUtil;
	@Mock
	private CustomOAuth2User customOAuth2User;
	@InjectMocks
	private AddressServiceImpl addressService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		addressService = new AddressServiceImpl(userService, addressRepository, aesUtil);
	}

	@Test
	@DisplayName("주소가 정상적으로 저장되는지 검증한다.")
	void createAddress() {
		AddressRequestDto addressRequestDto = AddressRequestDto.builder()
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.postalCode("14033")
			.category(AddressCategory.HOME)
			.isPrimary(true)
			.requestContent("노크 X")
			.build();
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();
		when(customOAuth2User.getName()).thenReturn("123123");
		when(userService.getUser("123123")).thenReturn(user);

		// when
		addressService.createAddress(addressRequestDto, customOAuth2User);

		// then
		verify(addressRepository, times(1)).save(any(Addresses.class));
	}

	@Test
	@DisplayName("주소가 정상적으로 수정되는지 검증한다.")
	void updateAddress() {
		//given
		Long addressId = 1L;
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses existingAddress = Addresses.builder()
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.postalCode("14033")
			.category(AddressCategory.HOME)
			.isPrimary(true)
			.requestContent("노크 X")
			.user(user)
			.build();

		when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));
		when(aesUtil.encryptToBase64("3동 309호")).thenReturn("암호화된 상세 주소");
		when(aesUtil.encryptToBase64("14085")).thenReturn("암호화된 우편 번호");

		AddressRequestDto request = AddressRequestDto.builder()
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.isPrimary(true)
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionInfo("왼쪽 골목")
			.build();

		CustomOAuth2User mockUser = mock(CustomOAuth2User.class);

		// when
		addressService.updateAddress(addressId, request, mockUser);

		// then
		verify(addressRepository).findById(addressId);
		verify(aesUtil).encryptToBase64("3동 309호");
		verify(aesUtil).encryptToBase64("14085");
		verify(addressRepository).save(existingAddress);

		assertThat(existingAddress.getAddressName()).isEqualTo("새 집");
		assertThat(existingAddress.getDetailAddress()).isEqualTo("암호화된 상세 주소");
		assertThat(existingAddress.getPostalCode()).isEqualTo("암호화된 우편 번호");
		assertThat(existingAddress.isPrimary()).isTrue();
		assertThat(existingAddress.getCategory()).isEqualTo(AddressCategory.COMPANY);
	}

	@Test
	@DisplayName("존재하지 않는 주소 id로 수정할 시 예외가 발생하는지 검증한다.")
	void updateAddress_notFound() {
		// given
		when(addressRepository.findById(999L)).thenReturn(Optional.empty());
		CustomOAuth2User mockUser = mock(CustomOAuth2User.class);

		AddressRequestDto request = AddressRequestDto.builder()
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.isPrimary(true)
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionInfo("왼쪽 골목")
			.build();

		// when & then
		assertThatThrownBy(() -> addressService.updateAddress(999L, request, mockUser))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("해당 주소 정보가 없습니다.");
	}
}