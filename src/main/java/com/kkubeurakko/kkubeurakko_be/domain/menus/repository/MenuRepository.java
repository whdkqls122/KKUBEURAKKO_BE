package com.kkubeurakko.kkubeurakko_be.domain.menus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Status;

@Repository
public interface MenuRepository extends JpaRepository<Menus, Long> {
	List<Menus> findByMenuGroupIdAndStatus(Long menuGroupId, Status status);
}
