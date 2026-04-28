package com.kkubeurakko.kkubeurakko_be.global.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;

public interface FailedTaskRepository extends JpaRepository<FailedTask, Long> {
}
