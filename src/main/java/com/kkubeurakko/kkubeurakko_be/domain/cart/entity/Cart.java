package com.kkubeurakko.kkubeurakko_be.domain.cart.entity;

import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "Cart")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Cart {
	@Id
	@GeneratedValue
	@Column(name = "cart_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_number")
	private Users user;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CartItem> cartItemList = new ArrayList<>();

	public void addCartItem(CartItem cartItem) {
		this.cartItemList.add(cartItem);
		cartItem.setCart(this);
	}

	public void removeCartItem(CartItem cartItem) {
		this.cartItemList.remove(cartItem);
	}

	public void clearItems() {
		this.cartItemList.clear();
	}

	public void revokeCart() {
		this.user = null;
	}
}
