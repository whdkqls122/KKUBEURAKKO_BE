package com.kkubeurakko.kkubeurakko_be.domain.stores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.stores.dto.StoreUpdateRequest;
import com.kkubeurakko.kkubeurakko_be.domain.stores.service.StoreService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/store")
@Tag(name = "Store API", description = "가게 운영 정보 API")
public class StoreController {
	private final StoreService storeService;

	@PatchMapping("/{storeId}operations")
	public ResponseEntity<Void> updateStore(
		@PathVariable Long storeId,
		@RequestBody StoreUpdateRequest request
	) {
		storeService.updateStoreBasicInfo(storeId, request);
		return ResponseEntity.ok().build();
	}
}
