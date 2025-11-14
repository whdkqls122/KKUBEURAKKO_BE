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

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.AddressCategory;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.addresses.repository.AddressRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
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
	@DisplayName("새 기본주소 생성 시 기존 기본주소가 false로 변경되는지 검증한다.")
	void createAddress_withExistingPrimary() {
		// given
		AddressRequestDto request = AddressRequestDto.builder()
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.postalCode("14033")
			.category(AddressCategory.HOME)
			.requestContent("노크 X")
			.build();

		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses oldPrimary = Addresses.builder()
			.id(1L)
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.isPrimary(true)
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionsInfo("왼쪽 골목")
			.build();

		when(customOAuth2User.getName()).thenReturn("123123");
		when(userService.getUser("123123")).thenReturn(user);
		when(addressRepository.countByUser(user)).thenReturn(1);
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.of(oldPrimary));

		// when
		addressService.createAddress(request, customOAuth2User);

		// then
		verify(addressRepository).save(any(Addresses.class));
		assertThat(oldPrimary.getIsPrimary()).isFalse();
	}

	@Test
	@DisplayName("기본주소가 정상적으로 업데이트 되는지 검증한다.")
	void updatePrimary() {
		//given
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses existingAddress = Addresses.builder()
			.id(1L)
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.postalCode("14033")
			.category(AddressCategory.HOME)
			.isPrimary(true)
			.requestContent("노크 X")
			.user(user)
			.build();

		Addresses newAddress = Addresses.builder()
			.id(2L)
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionsInfo("왼쪽 골목")
			.build();

		CustomOAuth2User mockUser = mock(CustomOAuth2User.class);
		when(mockUser.getName()).thenReturn("123123");

		when(addressRepository.findById(2L)).thenReturn(Optional.of(newAddress)); // 새 주소 조회 성공
		when(userService.getUser("123123")).thenReturn(user);
		when(addressRepository.existsByUser(user)).thenReturn(true);
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.of(existingAddress));

		// when
		addressService.updatePrimary(2L, mockUser);

		// then
		// 기존 주소는 false로, 새 주소는 true로 변경됐는지 확인
		assertThat(existingAddress.getIsPrimary()).isFalse();
		assertThat(newAddress.getIsPrimary()).isTrue();

		// 호출 검증
		verify(addressRepository).findById(2L);
		verify(addressRepository).findByUserAndIsPrimary(user, true);
		verify(userService).getUser("123123");
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

		AddressRequestDto request = AddressRequestDto.builder()
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionInfo("왼쪽 골목")
			.build();

		// when
		addressService.updateAddress(addressId, request);

		// then
		verify(addressRepository).findById(addressId);
		verify(aesUtil).encryptToBase64("3동 309호");
		verify(addressRepository).save(existingAddress);

		assertThat(existingAddress.getAddressName()).isEqualTo("새 집");
		assertThat(existingAddress.getDetailAddress()).isEqualTo("암호화된 상세 주소");
		assertThat(existingAddress.getIsPrimary()).isTrue();
		assertThat(existingAddress.getCategory()).isEqualTo(AddressCategory.COMPANY);
	}

	@Test
	@DisplayName("존재하지 않는 주소 id로 수정할 시 예외가 발생하는지 검증한다.")
	void updateAddress_notFound() {
		// given
		when(addressRepository.findById(999L)).thenReturn(Optional.empty());

		AddressRequestDto request = AddressRequestDto.builder()
			.addressName("새 집")
			.roadName("경기도 안양시 만안구 덕천로 140 (안양동, 준마아파트)")
			.detailAddress("3동 309호")
			.postalCode("14085")
			.category(AddressCategory.COMPANY)
			.requestContent("문앞에 두세요")
			.entrancePassword(null)
			.directionInfo("왼쪽 골목")
			.build();

		// when & then
		assertThatThrownBy(() -> addressService.updateAddress(999L, request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("해당 주소 정보가 없습니다.");
	}

	@Test
	@DisplayName("주소 조회가 정상적으로 이루어지는지 검증한다.")
	void getAllAddress() {
		//given
		String userNumber = "123123";
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses address1 = Addresses.builder()
			.id(1L)
			.addressName("우리집")
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.category(AddressCategory.HOME)
			.isPrimary(false)
			.user(user)
			.build();

		Addresses address2 = Addresses.builder()
			.id(2L)
			.addressName("회사")
			.roadName("서울특별시 강남구 테헤란로 427")
			.detailAddress("5층")
			.category(AddressCategory.COMPANY)
			.isPrimary(true)
			.user(user)
			.build();

		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
		when(addressRepository.findAllByUser(user)).thenReturn(List.of(address2, address1));

		when(aesUtil.decryptFromBase64("201호")).thenReturn("201호");
		when(aesUtil.decryptFromBase64("5층")).thenReturn("5층");

		// when
		List<AddressResponseDto> addressResponseDtoList = addressService.getAllAddresses(customOAuth2User);
		assertThat(addressResponseDtoList).hasSize(2);
		assertThat(addressResponseDtoList.get(0).addressName()).isEqualTo("회사");
		assertThat(addressResponseDtoList.get(0).address()).isEqualTo("서울특별시 강남구 테헤란로 427 5층");
		assertThat(addressResponseDtoList.get(0).isPrimary()).isTrue();
		assertThat(addressResponseDtoList.get(1).addressName()).isEqualTo("우리집");
		assertThat(addressResponseDtoList.get(1).address()).isEqualTo("안양로 168번길 16 201호");
		assertThat(addressResponseDtoList.get(1).isPrimary()).isFalse();

		verify(addressRepository, times(1)).findAllByUser(user);
		verify(aesUtil, times(2)).decryptFromBase64(anyString());
	}

	@Test
	@DisplayName("등록된 주소가 없을 경우 빈 리스트를 반환하는지 검증한다.")
	void getAllAddresses_emptyList() {
		// given
		String userNumber = "001010";
		Users user = Users.builder()
			.userNumber("001010")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
		when(addressRepository.findAllByUser(user)).thenReturn(List.of()); // 빈 리스트 반환

		// when
		List<AddressResponseDto> addressResponseDtoList = addressService.getAllAddresses(customOAuth2User);

		// then
		assertThat(addressResponseDtoList).isEmpty();
		verify(addressRepository, times(1)).findAllByUser(user);
	}

	@Test
	@DisplayName("기본 주소 조회가 정상적으로 이루어지는지 검증한다.")
	void getPrimaryAddress() {
		String userNumber = "123123";
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses primaryAddress = Addresses.builder()
			.id(1L)
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.addressName("우리 집")
			.isPrimary(true)
			.user(user)
			.build();

		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
		when(addressRepository.findByUserAndIsPrimary(user, true)).thenReturn(Optional.ofNullable(primaryAddress));
		when(aesUtil.decryptFromBase64("201호")).thenReturn("201호");

		// when
		AddressResponseDto addressResponseDto = addressService.getAddress(customOAuth2User);

		// then
		assertThat(addressResponseDto.addressName()).isEqualTo("우리 집");
		assertThat(addressResponseDto.address()).isEqualTo("안양로 168번길 16 201호");
		assertThat(addressResponseDto.isPrimary()).isTrue();

		verify(addressRepository, times(1)).findByUserAndIsPrimary(user, true);
	}

	@Test
	@DisplayName("기본 주소가 존재하지 않을 경우 예외가 발생하는지 검증한다.")
	void getPrimaryAddress_notFound() {
		// given
		String userNumber = "123123";
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();
		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
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
		String userNumber = "123123";
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		Addresses address = Addresses.builder()
			.id(1L)
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.addressName("우리 집")
			.isPrimary(true)
			.user(user)
			.build();

		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
		when(addressRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(address));
		when(aesUtil.decryptFromBase64("201호")).thenReturn("201호");

		// when
		AddressDetailResponseDto result = addressService.getAddressDetail(1L, customOAuth2User);

		// then
		assertThat(result.addressName()).isEqualTo("우리 집");
		assertThat(result.roadName()).isEqualTo("안양로 168번길 16");
		assertThat(result.detailAddress()).isEqualTo("201호");
		assertThat(result.isPrimary()).isTrue();

		verify(addressRepository, times(1)).findByIdAndUser(1L, user);
		verify(aesUtil, times(1)).decryptFromBase64("201호");
	}

	@Test
	@DisplayName("주소 상세 조회 시 해당 주소가 존재하지 않으면 예외가 발생하는지 검증한다.")
	void getAddressDetail_notFound() {
		// given
		String userNumber = "123123";
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		when(customOAuth2User.getName()).thenReturn(userNumber);
		when(userService.getUser(userNumber)).thenReturn(user);
		when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.getAddressDetail(99L, customOAuth2User))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("조회된 주소가 없습니다.");

		verify(addressRepository, times(1)).findByIdAndUser(99L, user);
	}

	@Test
	@DisplayName("주소가 정상적으로 삭제되는지 검증한다.")
	void deleteAddress_success() {
		// given
		Addresses address = Addresses.builder()
			.id(1L)
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.addressName("우리 집")
			.isPrimary(true)
			.build();

		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phoneHash("123124123412")
			.phone("1231234")
			.build();

		when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
		when(customOAuth2User.getName()).thenReturn(user.getUserNumber());
		when(userService.getUser(user.getUserNumber())).thenReturn(user);
		// when
		addressService.deleteAddress(1L, customOAuth2User);

		// then
		verify(addressRepository, times(1)).findById(1L);
		verify(addressRepository, times(1)).delete(address);
	}

	@Test
	@DisplayName("삭제하려는 주소가 존재하지 않을 경우 예외가 발생하는지 검증한다.")
	void deleteAddress_notFound() {
		// given
		when(addressRepository.findById(999L)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.deleteAddress(999L, customOAuth2User))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("조회된 주소가 없습니다.");
	}

	@Test
	@DisplayName("가장 오래된 주소가 정상적으로 삭제되는지 검증한다.")
	void deleteOldAddress() {
		// given
		Users user = Users.builder()
			.userNumber("123123")
			.role(UserRole.CUSTOMER)
			.phone("01012345678")
			.phoneHash("hashed")
			.build();

		Addresses oldAddress = Addresses.builder()
			.id(1L)
			.roadName("안양로 168번길 16")
			.detailAddress("201호")
			.addressName("오래된 주소")
			.isPrimary(false)
			.user(user)
			.build();

		when(customOAuth2User.getName()).thenReturn("123123");
		when(userService.getUser("123123")).thenReturn(user);
		when(addressRepository.findFirstByUserOrderByUpdateAtAsc(user)).thenReturn(Optional.of(oldAddress));

		// when
		addressService.deleteOldAddress(customOAuth2User);

		// then
		verify(addressRepository, times(1)).findFirstByUserOrderByUpdateAtAsc(user);
		verify(addressRepository, times(1)).delete(oldAddress);
	}
}