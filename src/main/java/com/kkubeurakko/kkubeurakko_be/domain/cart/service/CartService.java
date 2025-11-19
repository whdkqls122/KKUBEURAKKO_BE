package com.kkubeurakko.kkubeurakko_be.domain.cart.service;

import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartItemResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface CartService {

	List<CartItemResponseDto> getCartList(CustomOAuth2User customOAuth2User);

	void createCartItem(CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User);

	void updateCartItem(Long cartItemId, CustomOAuth2User customOAuth2User);

	void deleteCartItem(Long cartItemId, CustomOAuth2User customOAuth2User);
}
