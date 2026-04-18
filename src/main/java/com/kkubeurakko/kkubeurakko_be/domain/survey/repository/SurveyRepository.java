package com.kkubeurakko.kkubeurakko_be.domain.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.survey.entity.Survey;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

	boolean existsByUser_UserNumber(String userNumber);
}
