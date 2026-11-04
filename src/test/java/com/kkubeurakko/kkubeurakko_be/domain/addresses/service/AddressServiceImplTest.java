package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.TestFixture;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;
import com.kkubeurakko.kkubeurakko_be.global.security.util.AesUtil;

import jakarta.persistence.EntityNotFoundException;

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

	private Users user;
	private static final String USER_NUMBER = "123123";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		addressService = new AddressServiceImpl(userService, addressRepository, aesUtil);
		user = TestFixture.createUser(USER_NUMBER, "01012345678");
	}

	private void givenLoginUser() {
		when(customOAuth2User.getName()).thenReturn(USER_NUMBER);
		when(userService.getUser(USER_NUMBER)).thenReturn(user);
	}

	private AddressRequestDto createRequest() {
		return AddressRequestDto.builder()
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.postalCode("14033")
			.category(AddressCategory.HOME)
			.latitude(37.55961612576108)
			.longitude(126.9780770992189)
			.requestContent("노크 X")
			.build();
	}

	private AddressRequestDto updateRequest() {
		return AddressRequestDto.builder()
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.category(AddressCategory.COMPANY)
			.latitude(37.55961612576108)
			.longitude(126.9780770992189)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionsInfo("왼쪽 골목")
			.build();
	}

	@Test
	@DisplayName("주소가 정상적으로 저장되는지 검증한다.")
	void createAddress() {
		// given
		givenLoginUser();

		// when
		addressService.createAddress(createRequest(), customOAuth2User);

		// then
		verify(addressRepository, times(1)).save(any(Addresses.class));
	}

	@Test
	@DisplayName("새 기본주소 생성 시 기존 기본주소가 false로 변경되는지 검증한다.")
	void createAddress_withExistingPrimary() {
		// given
		Addresses oldPrimary = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.countByUser(user)).thenReturn(1);
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.of(oldPrimary));

		// when
		addressService.createAddress(createRequest(), customOAuth2User);

		// then
		verify(addressRepository).save(any(Addresses.class));
		assertThat(oldPrimary.getIsPrimary()).isFalse();
	}

	@Test
	@DisplayName("기본주소가 정상적으로 업데이트 되는지 검증한다.")
	void updatePrimary() {
		// given
		Addresses existingPrimary = TestFixture.createAddress(1L, user);
		Addresses newAddress = TestFixture.createAddress(2L, user);
		newAddress.updatePrimary(false);

		givenLoginUser();
		when(addressRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(newAddress));
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.of(existingPrimary));

		// when
		addressService.updatePrimary(2L, customOAuth2User);

		// then
		assertThat(existingPrimary.getIsPrimary()).isFalse();
		assertThat(newAddress.getIsPrimary()).isTrue();
		verify(addressRepository).findByIdAndUser(2L, user);
	}

	@Test
	@DisplayName("존재하지 않거나 본인의 주소가 아닌 주소를 기본 주소로 변경하면 예외가 발생하는지 검증한다.")
	void updatePrimary_notOwned() {
		// given
		givenLoginUser();
		when(addressRepository.findByIdAndUser(2L, user)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.updatePrimary(2L, customOAuth2User))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("조회된 주소가 없습니다.");

		verify(addressRepository, never()).findByUserAndIsPrimary(any(), anyBoolean());
	}

	@Test
	@DisplayName("주소가 정상적으로 수정되는지 검증한다.")
	void updateAddress() {
		// given
		Addresses existingAddress = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existingAddress));
		when(aesUtil.encryptToBase64("3동 309호")).thenReturn("암호화된 상세 주소");

		// when
		addressService.updateAddress(1L, updateRequest(), customOAuth2User);

		// then
		verify(addressRepository).findByIdAndUser(1L, user);
		verify(aesUtil).encryptToBase64("3동 309호");

		assertThat(existingAddress.getAddressName()).isEqualTo("새 집");
		assertThat(existingAddress.getDetailAddress()).isEqualTo("암호화된 상세 주소");
		assertThat(existingAddress.getIsPrimary()).isTrue();
		assertThat(existingAddress.getCategory()).isEqualTo(AddressCategory.COMPANY);
	}

	@Test
	@DisplayName("존재하지 않거나 본인의 주소가 아닌 주소를 수정하면 예외가 발생하는지 검증한다.")
	void updateAddress_notOwned() {
		// given
		givenLoginUser();
		when(addressRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.updateAddress(999L, updateRequest(), customOAuth2User))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("조회된 주소가 없습니다.");

		verify(aesUtil, never()).encryptToBase64(anyString());
	}

	@Test
	@DisplayName("주소 조회가 정상적으로 이루어지는지 검증한다.")
	void getAllAddress() {
		// given
		Addresses home = TestFixture.createAddress(1L, user);
		home.updatePrimary(false);

		Addresses company = Addresses.builder()
			.id(2L)
			.addressName("회사")
			.roadName("서울특별시 강남구 테헤란로 427")
			.detailAddress("5층")
			.category(AddressCategory.COMPANY)
			.isPrimary(true)
			.user(user)
			.build();

		givenLoginUser();
		when(addressRepository.findAllByUser(user)).thenReturn(List.of(company, home));
		when(aesUtil.decryptFromBase64("101호")).thenReturn("101호");
		when(aesUtil.decryptFromBase64("5층")).thenReturn("5층");
		when(aesUtil.decryptFromBase64("1234#")).thenReturn("1234#");

		// when
		List<AddressResponseDto> result = addressService.getAllAddresses(customOAuth2User);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).addressName()).isEqualTo("회사");
		assertThat(result.get(0).address()).isEqualTo("서울특별시 강남구 테헤란로 427 5층");
		assertThat(result.get(0).isPrimary()).isTrue();
		assertThat(result.get(1).addressName()).isEqualTo("우리집");
		assertThat(result.get(1).address()).isEqualTo("서울 강남구 테헤란로 101호");
		assertThat(result.get(1).isPrimary()).isFalse();
		assertThat(result.get(1).entrancePassword()).isEqualTo("1234#");

		verify(addressRepository, times(1)).findAllByUser(user);
	}

	@Test
	@DisplayName("등록된 주소가 없을 경우 빈 리스트를 반환하는지 검증한다.")
	void getAllAddresses_emptyList() {
		// given
		givenLoginUser();
		when(addressRepository.findAllByUser(user)).thenReturn(List.of());

		// when
		List<AddressResponseDto> result = addressService.getAllAddresses(customOAuth2User);

		// then
		assertThat(result).isEmpty();
		verify(addressRepository, times(1)).findAllByUser(user);
	}

	@Test
	@DisplayName("기본 주소 조회가 정상적으로 이루어지는지 검증한다.")
	void getPrimaryAddress() {
		// given
		Addresses primaryAddress = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.of(primaryAddress));
		when(aesUtil.decryptFromBase64("101호")).thenReturn("101호");

		// when
		AddressResponseDto result = addressService.getAddress(customOAuth2User);

		// then
		assertThat(result.addressName()).isEqualTo("우리집");
		assertThat(result.address()).isEqualTo("서울 강남구 테헤란로 101호");
		assertThat(result.isPrimary()).isTrue();

		verify(addressRepository, times(1)).findByUserAndIsPrimary(user, true);
	}

	@Test
	@DisplayName("기본 주소가 존재하지 않을 경우 예외가 발생하는지 검증한다.")
	void getPrimaryAddress_notFound() {
		// given
		givenLoginUser();
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.getAddress(customOAuth2User))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("조회된 기본 주소가 없습니다.");
	}


	@Test
	@DisplayName("주소 상세 조회가 정상적으로 이루어지는지 검증한다.")
	void getAddressDetail_success() {
		// given
		Addresses address = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(aesUtil.decryptFromBase64("101호")).thenReturn("101호");

		// when
		AddressDetailResponseDto result = addressService.getAddressDetail(1L, customOAuth2User);

		// then
		assertThat(result.addressName()).isEqualTo("우리집");
		assertThat(result.roadName()).isEqualTo("서울 강남구 테헤란로");
		assertThat(result.detailAddress()).isEqualTo("101호");
		assertThat(result.isPrimary()).isTrue();

		verify(addressRepository, times(1)).findByIdAndUser(1L, user);
	}

	@Test
	@DisplayName("존재하지 않거나 본인의 주소가 아닌 주소를 상세 조회하면 예외가 발생하는지 검증한다.")
	void getAddressDetail_notOwned() {
		// given
		givenLoginUser();
		when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.getAddressDetail(99L, customOAuth2User))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("조회된 주소가 없습니다.");
	}

	@Test
	@DisplayName("주소가 정상적으로 삭제되는지 검증한다.")
	void deleteAddress_success() {
		// given
		Addresses address = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));

		// when
		addressService.deleteAddress(1L, customOAuth2User);

		// then
		verify(addressRepository, times(1)).findByIdAndUser(1L, user);
		verify(addressRepository, times(1)).delete(address);
	}

	@Test
	@DisplayName("존재하지 않거나 본인의 주소가 아닌 주소를 삭제하면 예외가 발생하는지 검증한다.")
	void deleteAddress_notOwned() {
		// given
		givenLoginUser();
		when(addressRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.deleteAddress(999L, customOAuth2User))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("조회된 주소가 없습니다.");

		verify(addressRepository, never()).delete(any());
	}

	@Test
	@DisplayName("가장 오래된 주소가 정상적으로 삭제되는지 검증한다.")
	void deleteOldAddress() {
		// given
		Addresses oldAddress = TestFixture.createAddress(1L, user);

		givenLoginUser();
		when(addressRepository.findFirstByUserOrderByLastUsedAtAsc(user)).thenReturn(Optional.of(oldAddress));

		// when
		addressService.deleteOldAddress(customOAuth2User);

		// then
		verify(addressRepository, times(1)).findFirstByUserOrderByLastUsedAtAsc(user);
		verify(addressRepository, times(1)).delete(oldAddress);
	}
}