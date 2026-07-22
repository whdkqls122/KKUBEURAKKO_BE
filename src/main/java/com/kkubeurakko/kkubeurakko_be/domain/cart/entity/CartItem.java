package com.kkubeurakko.kkubeurakko_be.domain.cart.entity;

import java.util.ArrayList;
import java.util.List;

import com.kkubeurakko.kkubeurakko_be.domain.menus.entity.Menus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CartItem {

	@Id
	@GeneratedValue
	@Column(name = "cart_item_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id")
	private Menus menu;

	@Column(nullable = false)
	private int quantity;

	@OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CartItemOption> optionList = new ArrayList<>();

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public void addOption(CartItemOption cartItemOption) {
		this.optionList.add(cartItemOption);
		cartItemOption.setCartItem(this);
	}

	public void update(int quantity, List<CartItemOption> cartItemOptionList) {
		this.quantity = quantity;
		this.clearOptions();
		cartItemOptionList.forEach(this::addOption);
	}

	public void increaseQuantity(int quantity) {
		this.quantity += quantity;
	}

	private void clearOptions() {
		for (CartItemOption option : optionList) {
			option.setCartItem(null);
		}
		optionList.clear();
	}
}