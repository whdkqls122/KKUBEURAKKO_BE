package com.kkubeurakko.kkubeurakko_be.domain.menus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.service.MenuService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Menu API", description = "메뉴 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu")
public class MenuController {
	private final MenuService menuService;

	@Operation(
		summary = "메뉴 그룹별 메뉴 조회",
		description = "메뉴 그룹별 모든 메뉴를 조회합니다."
	)
	@GetMapping("/{menuGroupId}")
	public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getAllMenuByMenuGroup(@PathVariable Long menuGroupId) {
		List<MenuResponseDto> menuResponseDtoList = menuService.getAllMenuByMenuGroup(menuGroupId);

		return ResponseEntity.ok().body(ApiResponse.success(
			"메뉴 그룹별 메뉴 조회 성공",
			menuResponseDtoList
		));
	}

	@Operation(
		summary = "메뉴 상세 조회",
		description = "메뉴의 상세정보를 조회합니다."
	)
	@GetMapping("/{menuId}")
	public ResponseEntity<ApiResponse<MenuDetailResponseDto>> getMenuDetail(@PathVariable("menuId") Long menuId) {
		MenuDetailResponseDto menuDetailResponseDto = menuService.getMenuDetail(menuId);
		return ResponseEntity.ok().body(ApiResponse.success(
			"메뉴 상세조회에 성공했습니다.",
			menuDetailResponseDto
		));
	}
}
