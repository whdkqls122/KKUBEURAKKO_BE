package com.kkubeurakko.kkubeurakko_be.domain.menus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupRequestDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupResponseDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupUpdateDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.MenuGroupWithMenusDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.service.MenuGroupService;
import com.kkubeurakko.kkubeurakko_be.global.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

	@Operation(
		summary = "메뉴 그룹 생성",
		description = "새로운 메뉴 그룹을 등록합니다."
	)
	@PostMapping
	public ResponseEntity<String> createMenuGroup(@Valid @RequestBody MenuGroupRequestDto requestDto) {
		menuGroupService.createMenuGroup(requestDto);
		return ResponseEntity.ok("메뉴 그룹 생성에 성공하였습니다.");
	}

	@Operation(
		summary = "메뉴 그룹 수정",
		description = "메뉴 그룹 이름과 설명을 수정합니다."
	)
	@PutMapping
	public ResponseEntity<ApiResponse<Void>> updateMenuGroup(@Valid @RequestBody MenuGroupUpdateDto requestDto) {
		menuGroupService.updateMenuGroup(requestDto);
		return ResponseEntity.ok(ApiResponse.success("메뉴 그룹 수정이 완료되었습니다.", null));
	}

	@Operation(
		summary = "메뉴 그룹 상태 변경",
		description = "메뉴 그룹의 상태를 변경합니다. "
	)
	@PatchMapping("/{menuGroupId}/status")
	public ResponseEntity<ApiResponse<Void>> updateMenuGroupStatus(
		@PathVariable Long menuGroupId,
		@RequestParam MenuStatus menuStatus) {
		menuGroupService.updateMenuGroupStatus(menuGroupId, menuStatus);
		return ResponseEntity.ok(ApiResponse.success("메뉴 그룹 상태가 변경되었습니다.", null));
	}

	@Operation(
		summary = "메뉴 그룹 전체 조회",
		description = "메뉴 그룹과 그룹에 포함되는 메뉴를 전체 조회합니다. "
	)
	@GetMapping("/{storeId}")
	public ResponseEntity<List<MenuGroupWithMenusDto>> getAllMenuGroups(@PathVariable Long storeId) {
		List<MenuGroupWithMenusDto> result = menuGroupService.getAllMenuGroupsWithMenus(storeId);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/{menuGroupId}")
	public ResponseEntity<Void> deleteMenuGroup(@PathVariable Long menuGroupId) {
		menuGroupService.deleteMenuGroup(menuGroupId);
		return ResponseEntity.noContent().build();
	}
}
