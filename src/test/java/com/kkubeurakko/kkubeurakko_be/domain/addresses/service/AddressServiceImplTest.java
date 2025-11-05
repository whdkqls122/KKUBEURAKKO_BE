package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import static org.mockito.Mockito.*;

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
}