package com.kkubeurakko.kkubeurakko_be.domain.survey.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "survey")
@Builder
public class Survey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Q1
	@Column(length = 150)
	private String improvement;

	// Q3
	@Column(length = 150)
	private String inconvenience;

	@Column(nullable = false)
	private boolean rewarded;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SurveyOption> options = new ArrayList<>();

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number", nullable = false, unique = true)
	private Users user;

	public void markRewarded() {
		this.rewarded = true;
	}
}