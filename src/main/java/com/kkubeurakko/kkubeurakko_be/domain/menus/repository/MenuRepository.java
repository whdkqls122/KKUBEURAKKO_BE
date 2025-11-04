package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;

@Repository
public interface MenuRepository extends JpaRepository<Menus, Long> {
	List<Menus> findByMenuGroupIdAndStatus(Long menuGroupId, Status status);

	// @EntityGraph(attributePaths =
	// 	{"optionGroupList", "optionGroupList.options"},
	// 	type = EntityGraph.EntityGraphType.FETCH)
	@Query("select m from Menus m "
		+ "left join fetch m.optionGroupList og on og.status = com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE "
		+ "left join fetch og.options o on o.status = com.kkubeurakko.kkubeurakko_be.domain.options.entity.Status.ON_SALE "
		+ "where m.id = :menuId")
	Optional<Menus> findMenuDetail(@Param("menuId") Long menuId);
}
