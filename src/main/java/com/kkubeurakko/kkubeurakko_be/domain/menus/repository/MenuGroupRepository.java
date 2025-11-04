package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuGroups;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;

@Repository
public interface MenuGroupRepository extends JpaRepository<MenuGroups, Long> {
	List<MenuGroups> findByStatus(MenuStatus menuStatus);
}
