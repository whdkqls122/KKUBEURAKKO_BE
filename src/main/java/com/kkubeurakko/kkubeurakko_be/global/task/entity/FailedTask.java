package com.kkubeurakko.kkubeurakko_be.global.task.entity;

import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "failed_task")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class FailedTask extends BaseEntity {
	@Id
	@GeneratedValue
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "task_type", nullable = false)
	private TaskType taskType;
	@Lob
	@Column(name = "payload")
	private String payload;
	@Column(name = "retry_count", nullable = false)
	private int retryCount;
	@Enumerated(EnumType.STRING)
	@Column(name = "task_status", nullable = false)
	private TaskStatus taskStatus;
	@Column(name = "last_error_message")
	private String lastErrorMessage;
	@Column(name = "last_error_type")
	private String lastErrorType;
}
