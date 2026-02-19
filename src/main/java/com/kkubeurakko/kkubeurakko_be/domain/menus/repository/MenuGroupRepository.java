package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface MenuGroupRepository extends JpaRepository<MenuGroups, Long> {
	List<MenuGroups> findByMenuStatusAndActiveTrue(MenuStatus menuStatus);

	@Query("""
		    SELECT mg FROM MenuGroups mg
		    LEFT JOIN FETCH mg.menus m
		    WHERE mg.store.id = :storeId
		    AND mg.active
		""")
	List<MenuGroups> findAllByStoreIdWithMenus(@Param("storeId") Long storeId);
}
