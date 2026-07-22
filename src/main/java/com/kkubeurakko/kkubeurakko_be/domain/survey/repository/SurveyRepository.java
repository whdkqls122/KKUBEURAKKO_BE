package com.kkubeurakko.kkubeurakko_be.domain.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

	boolean existsByUser(Users user);

	@Query("select s.id from Survey s where s.rewarded = false")
	List<Long> findIdsByRewardedFalse();
}
