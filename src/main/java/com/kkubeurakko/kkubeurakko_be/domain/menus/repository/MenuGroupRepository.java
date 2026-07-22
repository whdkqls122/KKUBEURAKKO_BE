package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

@Repository
public interface MenuGroupRepository extends JpaRepository<MenuGroups, Long> {
	List<MenuGroups> findByMenuStatusNotIn(List<MenuStatus> statuses);

	@Query("""
		  SELECT DISTINCT mg
		  FROM MenuGroups mg
		  LEFT JOIN FETCH mg.menus m
		  WHERE mg.store.id = :storeId
		    AND mg.menuStatus <> :deleted
		""")
	List<MenuGroups> findAllByStoreIdWithMenus(@Param("storeId") Long storeId, @Param("deleted") MenuStatus deleted);
}
