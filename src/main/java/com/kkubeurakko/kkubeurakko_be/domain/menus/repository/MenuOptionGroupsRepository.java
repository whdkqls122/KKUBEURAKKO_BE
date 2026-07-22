package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.MenuOptionGroups;

@Repository
public interface MenuOptionGroupsRepository extends JpaRepository<MenuOptionGroups, Long> {
}
