package com.kkubeurakko.kkubeurakko_be.domain.menus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.service.MenuGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "MenuGroup API", description = "메뉴 그룹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/menu/menu-group")
public class MenuGroupController {
	private final MenuGroupService menuGroupService;

	@Operation(
		summary = "메뉴 그룹 이름 조회",
		description = "모든 메뉴 그룹 이름을 조회합니다."
	)
	@GetMapping()
	public ResponseEntity<ApiResponse<List<MenuGroupResponseDto>>> getAllMenuGroupName() {
		List<MenuGroupResponseDto> menuGroupResponseDtoList = menuGroupService.getAllMenuGroupName();
		return ResponseEntity.ok().body(ApiResponse.success(
			"메뉴 그룹 조회 성공",
			menuGroupResponseDtoList
		));
	}
}
