package com.kkubeurakko.kkubeurakko_be.domain.options.repository;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroups, Long> {

}
