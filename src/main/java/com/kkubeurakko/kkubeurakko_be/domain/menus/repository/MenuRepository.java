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

	@Query(
		"select m "
			+ "from Menus m "
			+ "left join fetch m.optionGroupList og"
			+ " left join fetch og.options "
			+ "where m.id = :menuId"
	)
	Optional<Menus> findMenuDetail(@Param("menuId") Long menuId);
}
