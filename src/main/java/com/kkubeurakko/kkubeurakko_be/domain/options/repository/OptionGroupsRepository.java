package com.kkubeurakko.kkubeurakko_be.domain.options.repository;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionGroupsRepository extends JpaRepository<OptionGroups, Long> {

}
