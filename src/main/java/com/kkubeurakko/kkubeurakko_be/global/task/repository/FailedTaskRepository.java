package com.kkubeurakko.kkubeurakko_be.global.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkubeurakko.kkubeurakko_be.global.task.entity.FailedTask;
import com.kkubeurakko.kkubeurakko_be.global.task.entity.TaskStatus;

public interface FailedTaskRepository extends JpaRepository<FailedTask, Long> {
	List<FailedTask> findFailedTaskByTaskStatus(TaskStatus taskStatus);
}
