package com.kkubeurakko.kkubeurakko_be.domain.options.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.options.entity.OptionGroups;
import com.kkubeurakko.kkubeurakko_be.domain.options.entity.Options;

@Repository
public interface OptionRepository extends JpaRepository<Options, Long> {
	List<Options> findByOptionGroup(OptionGroups optionGroup);

}
