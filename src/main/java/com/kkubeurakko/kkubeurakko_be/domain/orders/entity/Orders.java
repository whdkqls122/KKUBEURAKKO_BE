package com.kkubeurakko.kkubeurakko_be.domain.orders.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.kkubeurakko.kkubeurakko_be.domain.payments.entity.Payments;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import jakarta.persistence.OneToOne;
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

	@Column(name = "order_number")
	private String orderNumber;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "order_type", nullable = false)
	private OrderType orderType;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	List<OrderItems> orderItemList = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number")
	private Users user;

	@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
	private Payments payments;

	@Embedded
	private OrderAddressSnapshot addressSnapshot;

	public void revokeUser() {
		this.user = null;
	}

	public void accept(int completionTime) {
		if (this.status != Status.PENDING) {
			throw new IllegalStateException("접수할 수 없는 상태입니다.");
		}
		this.status = Status.COOKING;
		this.completionTime = completionTime;
	}

	public void startDelivery() {
		if (this.status != Status.COOKING) {
			throw new IllegalStateException("조리 중인 주문만 배달 시작할 수 있습니다.");
		}
		this.status = Status.ON_DELIVERY;
	}

	public void complete() {
		if (this.status != Status.COOKING) {
			throw new IllegalStateException("조리 중인 주문만 완료할 수 있습니다.");
		}
		this.status = Status.COMPLETED;
	}

	public void cancel() {
		this.status = Status.CANCELED;
	}

	public void refund() {
		this.status = Status.REFUNDED;
	}

}
