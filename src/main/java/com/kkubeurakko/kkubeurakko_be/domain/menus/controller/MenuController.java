package com.kkubeurakko.kkubeurakko_be.domain.menus.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuDetailDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.AdminMenuUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuDetailResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.service.MenuService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	@GetMapping("/detail/{menuId}")
	public ResponseEntity<ApiResponse<MenuDetailResponseDto>> getMenuDetail(@PathVariable("menuId") Long menuId) {
		MenuDetailResponseDto menuDetailResponseDto = menuService.getMenuDetail(menuId);
		return ResponseEntity.ok().body(ApiResponse.success(
			"메뉴 상세조회에 성공했습니다.",
			menuDetailResponseDto
		));
	}

	@Operation(
		summary = "관리자 메뉴 단일 조회",
		description = "관리자 측에서 메뉴를 조회합니다."
	)
	@GetMapping("/detail/admin/{menuId}")
	public ResponseEntity<ApiResponse<AdminMenuDetailDto>> getMenuDetailAdmin(@PathVariable Long menuId) {
		AdminMenuDetailDto adminMenuDetailDto = menuService.getMenuDetailForAdmin(menuId);
		return ResponseEntity.ok().body(ApiResponse.success(
			"관리자 메뉴 상세조회에 성공했습니다.",
			adminMenuDetailDto
		));
	}

	@Operation(
		summary = "메뉴 추가",
		description = "관리자가 새로운 메뉴를 등록합니다."
	)
	@PostMapping(
		value = "/admin",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<ApiResponse<Void>> createMenu(
		@Valid @RequestPart("menuRequest") AdminMenuRequestDto request,
		@RequestPart(value = "image", required = false) MultipartFile image
	) throws IOException {
		menuService.createMenu(request, image);

		return ResponseEntity.ok().body(
			ApiResponse.success(
				"메뉴가 정상적으로 등록되었습니다.",
				null
			)
		);
	}

	@Operation(
		summary = "관리자 메뉴 수정",
		description = "관리자 측에서 메뉴를 수정합니다."
	)
	@PutMapping("/admin/{menuId}")
	public ResponseEntity<ApiResponse<Void>> updateMenu(
		@PathVariable Long menuId,
		@Valid @RequestBody AdminMenuUpdateDto request
	) {
		menuService.updateMenu(menuId, request);
		return ResponseEntity.ok(ApiResponse.success("메뉴가 수정되었습니다.", null));
	}

	@Operation(
		summary = "관리자 메뉴 상태 변경",
		description = "관리자 측에서 메뉴의 상태를 변경합니다."
	)
	@PatchMapping("/admin/{menuId}/status")
	public ResponseEntity<ApiResponse<Void>> changeMenuStatus(
		@PathVariable Long menuId,
		@RequestParam MenuStatus status
	) {
		menuService.changeMenuStatus(menuId, status);
		return ResponseEntity.ok(ApiResponse.success("메뉴 상태가 변경되었습니다.", null));
	}
}
