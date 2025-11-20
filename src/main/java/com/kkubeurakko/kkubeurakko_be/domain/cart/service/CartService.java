package com.kkubeurakko.kkubeurakko_be.domain.cart.service;

import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.cart.dto.CartResponseDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface CartService {

	CartResponseDto getCartList(CustomOAuth2User customOAuth2User);

	void createCartItem(CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User);

	void updateCartItem(Long cartItemId, CartRequestDto cartRequestDto, CustomOAuth2User customOAuth2User);

	void deleteCartItem(Long cartItemId, CustomOAuth2User customOAuth2User);
}
