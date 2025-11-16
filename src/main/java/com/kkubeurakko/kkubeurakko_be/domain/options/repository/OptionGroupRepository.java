package com.kkubeurakko.kkubeurakko_be.domain.options.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroups, Long> {

	@Query("""
		    SELECT DISTINCT og 
		    FROM OptionGroups og
		    JOIN FETCH og.options o
		    JOIN og.menuOptionGroups mog
		    WHERE mog.menu.id = :menuId
		""")
	List<OptionGroups> findAllByMenuId(@Param("menuId") Long menuId);

}
