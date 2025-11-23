package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Orders")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "orders_id")
	private Long id;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@Column(name = "order_date", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime orderDate;

	@Column(name = "completion_time")
	private int completionTime;

	@Column(name = "store_request", length = 100)
	private String storeRequest;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Column(name = "address", length = 100)
	private String address;

	@OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	List<OrderItems> orderItemList = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number", nullable = false)
	private Users user;
}
