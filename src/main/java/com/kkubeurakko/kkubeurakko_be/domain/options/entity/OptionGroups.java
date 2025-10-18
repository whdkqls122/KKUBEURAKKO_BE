package com.kkubeurakko.kkubeurakko_be.domain.options.entity;

import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;
import com.kkubeurakko.kkubeurakko_be.global.BaseEntity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

	@Column(name = "selected_num", nullable = true)
	private int selectedNum;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menus_id", nullable = true)
	private Menus menu;

	@OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default //테스트 코드시 초기화하기 위함
	private List<Options> options = new ArrayList<>();

}
