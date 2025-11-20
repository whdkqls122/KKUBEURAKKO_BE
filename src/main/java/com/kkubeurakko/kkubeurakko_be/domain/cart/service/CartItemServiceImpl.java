package com.kkubeurakko.kkubeurakko_be.domain.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartItemResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.Cart;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItem;
import com.kkubeurakko.kkubeurakko_be.domain.cart.entity.CartItemOption;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartItemOptionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartItemRepository;
import com.kkubeurakko.kkubeurakko_be.domain.cart.repository.CartRepository;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.repository.MenuRepository;
import com.kkubeurakko.kkubeurakko_be.domain.options.repository.OptionRepository;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;
import com.kkubeurakko.kkubeurakko_be.domain.users.service.UserService;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartService {
	private final CartRepository cartRepository;
	private final UserService userService;
	private final MenuRepository menuRepository;
	private final OptionRepository optionRepository;
	private final CartItemRepository cartItemRepository;
	private final CartItemOptionRepository cartItemOptionRepository;

	@Override
	public List<CartItemResponseDto> getCartList(CustomOAuth2User customOAuth2User) {
		return null;
	}

	//메서드 정리하기
	@Override
	@Transactional
	public void createCartItem(CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		Cart cart = cartRepository.findByUser(user)
			.orElseGet(() -> {
				Cart newCart = Cart.builder()
					.user(user)
					.build();
				return cartRepository.save(newCart);
			});

		Menus menu = menuRepository.findById(cartRequestDto.menuId())
			.orElseThrow(() -> new IllegalArgumentException("조회된 메뉴가 없습니다."));

		CartItem cartItem = toEntity(cart, menu, cartRequestDto);
		cart.addCartItem(cartItem);

		cartItemRepository.save(cartItem);

		List<CartItemOption> cartItemOptionList = toEntity(cartItem, cartRequestDto);
		cartItemOptionList.forEach(cartItem::addOption);

		cartItemOptionRepository.saveAll(cartItemOptionList);
	}

	@Override
	public void updateCartItem(Long cartItemId, CustomOAuth2User customOAuth2User) {

	}

	@Override
	public void deleteCartItem(Long cartItemId, CustomOAuth2User customOAuth2User) {

	}

	private CartItem toEntity(Cart cart, Menus menu, CartRequestDto cartRequestDto) {
		return CartItem.builder()
			.cart(cart)
			.menu(menu)
			.quantity(cartRequestDto.quantity())
			.build();
	}

	private List<CartItemOption> toEntity(CartItem cartItem, CartRequestDto cartRequestDto) {
		return optionRepository.findAllById(cartRequestDto.optionIdList())
			.stream()
			.map(option ->
				CartItemOption.builder()
					.cartItem(cartItem)
					.option(option)
					.build()
			)
			.toList();
	}
}
