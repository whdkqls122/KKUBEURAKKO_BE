package com.kkubeurakko.kkubeurakko_be.domain.menus.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Menus")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menus extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "menus_id")
	private Long id;

	@Column(name = "name", nullable = false, length = 24)
	private String name;

	@Column(name = "description", length = 120)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "img", nullable = false, length = 500)
	private String img;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_groups_id", nullable = false)
	private MenuGroups menuGroup;

}
