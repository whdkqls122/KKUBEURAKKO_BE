package com.kkubeurakko.kkubeurakko_be.domain.cart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartItemQuantityResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartItemOptionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartItemRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.dto.UserDto;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.UserRole;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
	@Mock
	private CartRepository cartRepository;
	@Mock
	private UserService userService;
	@Mock
	private MenuRepository menuRepository;
	@Mock
	private OptionRepository optionRepository;
	@Mock
	private CartItemRepository cartItemRepository;
	@Mock
	private CartItemOptionRepository cartItemOptionRepository;

	@InjectMocks
	private CartServiceImpl cartService;

	private Users user;
	private CustomOAuth2User oAuth2User;
	private Menus menu;
	private Options option;
	private UserDto userDto;

	@BeforeEach
	void setUp() {
		user = Users.builder()
			.userNumber("u1q23jok12j")
			.phone("01012345678")
			.phoneHash("hashedPhone")
			.role(UserRole.CUSTOMER)
			.build();

		userDto = UserDto.builder()
			.userNumber(user.getUserNumber())
			.role(UserRole.CUSTOMER)
			.firstLogin(false)
			.build();
		oAuth2User = new CustomOAuth2User(userDto);

		menu = Menus.builder()
			.id(1L)
			.name("Pizza")
			.price(10000)
			.build();

		option = Options.builder()
			.id(1L)
			.name("Extra Cheese")
			.price(2000)
			.build();
	}

	@Test
	@DisplayName("정상적으로 장바구니 목록이 조회되는지 검증한다.")
	void getCartList() {
		Cart cart = Cart.builder().id(1L).user(user).build();
		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartRepository.findByUserNumber(user.getUserNumber())).thenReturn(Optional.of(cart));

		CartResponseDto response = cartService.getCartList(oAuth2User);

		assertNotNull(response);
		assertEquals(1L, response.cartId());
	}

	@Test
	@DisplayName("장바구니가 생성되지 않았을 경우 장바구니 생성 후 정상적으로 반환되는지 검증한다.")
	void getCartList_WhenCartDoesNotExist() {
		Cart cart = Cart.builder().id(1L).user(user).build();
		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartRepository.findByUserNumber(user.getUserNumber())).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);

		CartResponseDto response = cartService.getCartList(oAuth2User);

		assertNotNull(response);
		assertEquals(1L, response.cartId());
	}

	@Test
	@DisplayName("장바구니가 정상적으로 생성되고 메뉴가 담기는지 검증한다.")
	void createCartItem() {
		CartRequestDto requestDto = new CartRequestDto(1L, 2, List.of(option.getId()));
		Cart cart = Cart.builder().id(1L).user(user).build();

		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartRepository.findByUserNumber(user.getUserNumber())).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		when(menuRepository.findById(requestDto.menuId())).thenReturn(Optional.of(menu));
		when(optionRepository.findAllById(requestDto.optionIdList())).thenReturn(List.of(option));

		cartService.createCartItem(requestDto, oAuth2User);

		verify(cartItemRepository, times(1)).save(any(CartItem.class));
		verify(cartItemOptionRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("장바구니 업데이트가 정상적으로 되는지 검증한다.")
	void updateCartItem() {
		CartRequestDto requestDto = new CartRequestDto(1L, 3, List.of(option.getId()));
		CartItem cartItem = CartItem.builder()
			.id(1L)
			.cart(Cart.builder().user(user).build())
			.menu(menu)
			.quantity(2)
			.build();

		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
		when(optionRepository.findAllById(requestDto.optionIdList())).thenReturn(List.of(option));

		cartService.updateCartItem(1L, requestDto, oAuth2User);

		assertEquals(3, cartItem.getQuantity());
		assertEquals(1, cartItem.getOptionList().size());
	}

	@Test
	@DisplayName("장바구니에서 메뉴가 정상적으로 제거되는지 검증한다.")
	void deleteCartItem() {
		Cart cart = Cart.builder().id(1L).user(user).build();
		CartItem cartItem = CartItem.builder()
			.id(1L)
			.cart(cart)
			.menu(menu)
			.quantity(2)
			.build();

		cart.addCartItem(cartItem);

		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

		cartService.deleteCartItem(1L, oAuth2User);

		assertTrue(cart.getCartItemList().isEmpty());
	}

	@Test
	@DisplayName("장바구니 총 수량이 정상적으로 조회되는지 검증한다.")
	void getCartItemQuantity() {
		// given
		when(userService.getUser(oAuth2User.getName())).thenReturn(user);
		when(cartItemRepository.sumQuantityByUserNumber(user.getUserNumber()))
			.thenReturn(5L);

		// when
		CartItemQuantityResponseDto response =
			cartService.getCartItemQuantity(oAuth2User);

		// then
		assertNotNull(response);
		assertEquals(5L, response.cartItemQuantity());

		verify(userService, times(1)).getUser(oAuth2User.getName());
		verify(cartItemRepository, times(1))
			.sumQuantityByUserNumber(user.getUserNumber());
	}

}