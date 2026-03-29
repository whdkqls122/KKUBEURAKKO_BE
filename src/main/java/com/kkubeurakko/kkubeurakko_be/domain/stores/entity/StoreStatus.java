package com.kkubeurakko.kkubeurakko_be.domain.stores.entity;

public enum StoreStatus {

	OPEN("영업중"),
	CLOSED("영업종료"),
	PAUSED("일시중지");

	private final String description;

	StoreStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}