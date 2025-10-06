package com.kkubeurakko.kkubeurakko_be.domain.options.repository;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionsRepository extends JpaRepository<Options, Long> {
    List<Options> findByOptionGroup(OptionGroups optionGroup);
}
