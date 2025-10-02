package com.kkubeurakko.kkubeurakko_be.domain.menus.entity;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "MenuGroups")
@Getter
public class MenuGroups extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "menu_groups_id")
	private Long id;

	@Column(name = "name", nullable = false, length = 70)
	private String name;

	@Column(name = "description", length = 250)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stroes_id", nullable = false)
	private Stores store;
}
