package com.kkubeurakko.kkubeurakko_be.domain.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.CLASS,
	include = JsonTypeInfo.As.PROPERTY,
	property = "@class"
)

public interface TempUser {
	String getUserNumber();

	String getNickname();

	@JsonIgnore
	boolean isOAuth();
}
