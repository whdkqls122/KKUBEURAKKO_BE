package com.kkubeurakko.kkubeurakko_be.domain.addresses.service;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.dto.AddressRequestDto;
import com.kkubeurakko.kkubeurakko_be.global.oauth.dto.CustomOAuth2User;

public interface AddressService {
	void createAddress(AddressRequestDto addressRequestDto, CustomOAuth2User customOAuth2User);
}
