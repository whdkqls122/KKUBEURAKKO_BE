package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionStatus;

@Repository
public interface MenuRepository extends JpaRepository<Menus, Long> {
	List<Menus> findByMenuGroupIdAndMenuStatus(Long menuGroupId, MenuStatus menuStatus);

	@Query("""
		    select distinct m from Menus m
		    join fetch m.menuOptionGroups mog
		    join fetch mog.optionGroup og
		    left join fetch og.options o
		    where m.id = :menuId
		      and og.optionStatus = :status
		""")
	Optional<Menus> findMenuDetail(Long menuId, OptionStatus status);

}
