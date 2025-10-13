package com.kkubeurakko.kkubeurakko_be.domain.options.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OptionGroups")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
public class OptionGroups extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "option_groups_id")
	private Long id;

	@Column(name = "name", nullable = false, length = 30)
	private String name;

	@Column(name = "description", length = 250)
	private String description;

	@Column(name = "required", nullable = false)
	private boolean required;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Enumerated(EnumType.STRING)
	@Column(name = "option_group_type", nullable = false)
	private OptionGroupType optionGroupType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menus_id", nullable = false)
	private Menus menu;

	@OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Options> options = new ArrayList<>();

}
