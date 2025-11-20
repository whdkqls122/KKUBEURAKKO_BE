package com.kkubeurakko.kkubeurakko_be.domain.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartItemOptionResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartItemResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartResponseDto;
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
public class CartServiceImpl implements CartService {
	private final CartRepository cartRepository;
	private final UserService userService;
	private final MenuRepository menuRepository;
	private final OptionRepository optionRepository;
	private final CartItemRepository cartItemRepository;
	private final CartItemOptionRepository cartItemOptionRepository;

	@Override
	@Transactional(readOnly = true)
	public CartResponseDto getCartList(CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		Cart cart = cartRepository.findByUser(user)
			.orElseGet(() -> cartRepository.save(
					Cart.builder()
						.user(user)
						.build()
				)
			);

		return CartResponseDto.builder()
			.cartId(cart.getId())
			.cartItemResponseDtoList(toResponseDto(cart))
			.build();
	}

	//메서드 정리하기
	@Override
	@Transactional
	public void createCartItem(CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		Cart cart = cartRepository.findByUser(user)
			.orElseGet(() -> createCartForUser(user));

		Menus menu = menuRepository.findById(cartRequestDto.menuId())
			.orElseThrow(() -> new IllegalArgumentException("조회된 메뉴가 없습니다."));

		if (increaseQuantityIfExists(cart, menu, cartRequestDto)) {
			return;
		}

		createNewCartItem(cart, menu, cartRequestDto);
	}

	@Override
	@Transactional
	public void updateCartItem(Long cartItemId, CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());
		CartItem cartItem = cartItemRepository.findById(cartItemId)
			.orElseThrow(() -> new IllegalArgumentException("조회된 장바구니 메뉴가 없습니다."));

		if (!cartItem.getCart().getUser().equals(user)) {
			throw new IllegalArgumentException("본인의 장바구니가 아닙니다.");
		}
		List<CartItemOption> cartItemOptionList = toEntity(cartItem, cartRequestDto);

		cartItem.update(cartRequestDto.quantity(), cartItemOptionList);
	}

	@Override
	@Transactional
	public void deleteCartItem(Long cartItemId, CustomOAuth2User customOAuth2User) {
		Users user = userService.getUser(customOAuth2User.getName());

		CartItem cartItem = cartItemRepository.findById(cartItemId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 메뉴입니다."));

		Cart cart = cartItem.getCart();

		if (!cart.getUser().equals(user)) {
			throw new IllegalArgumentException("본인의 장바구니가 아닙니다.");
		}

		cart.removeCartItem(cartItem);
	}

	private Cart createCartForUser(Users user) {
		Cart cart = Cart.builder()
			.user(user)
			.build();
		return cartRepository.save(cart);
	}

	private boolean increaseQuantityIfExists(Cart cart, Menus menu, CartRequestDto cartRequestDto) {
		for (CartItem existItem : cart.getCartItemList()) {
			if (existItem.getMenu().equals(menu)) {
				List<Long> existOpionIdList = existItem.getOptionList().stream()
					.map(opt -> opt.getOption().getId())
					.sorted()
					.toList();

				List<Long> newOptionIdList = cartRequestDto.optionIdList().stream()
					.sorted()
					.toList();

				if (existOpionIdList.equals(newOptionIdList)) {
					existItem.update(
						existItem.getQuantity() + cartRequestDto.quantity(),
						existItem.getOptionList()
					);
					return true;
				}
			}
		}
		return false;
	}

	private void createNewCartItem(Cart cart, Menus menu, CartRequestDto cartRequestDto) {
		CartItem cartItem = toEntity(cart, menu, cartRequestDto);
		cart.addCartItem(cartItem);
		cartItemRepository.save(cartItem);

		List<CartItemOption> cartItemOptionList = toEntity(cartItem, cartRequestDto);
		cartItemOptionList.forEach(cartItem::addOption);
		cartItemOptionRepository.saveAll(cartItemOptionList);
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

	private List<CartItemResponseDto> toResponseDto(Cart cart) {
		return cart.getCartItemList().stream()
			.map(cartItem -> {
				List<CartItemOptionResponseDto> options = cartItem.getOptionList().stream()
					.map(opt -> CartItemOptionResponseDto.builder()
						.optionId(opt.getOption().getId())
						.optionName(opt.getOption().getName())
						.price(opt.getOption().getPrice())
						.build()
					)
					.toList();

				int totalPrice = (
					cartItem.getMenu().getPrice() +
						options.stream().mapToInt(CartItemOptionResponseDto::price).sum()
				)
					* cartItem.getQuantity();

				return CartItemResponseDto.builder()
					.cartItemId(cartItem.getId())
					.menuId(cartItem.getMenu().getId())
					.menuName(cartItem.getMenu().getName())
					.quantity(cartItem.getQuantity())
					.totalPrice(totalPrice)
					.optionResponseDtoList(options)
					.build();
			})
			.toList();
	}
}
