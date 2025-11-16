package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuStatus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;

@Repository
public interface MenuRepository extends JpaRepository<Menus, Long> {
	List<Menus> findByMenuGroupIdAndMenuStatus(Long menuGroupId, MenuStatus menuStatus);
}
