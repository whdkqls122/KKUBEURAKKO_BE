package com.kkubeurakko.kkubeurakko_be.domain.auth.util;

public class UserIdMaskingUtil {
	public static String mask(String userId) {
		if (userId == null || userId.length() < 4) {
			return userId;
		}

		int length = userId.length();
		String prefix = userId.substring(0, 2);
		String suffix = userId.substring(length - 2);

		return prefix + "*".repeat(length - 4) + suffix;
	}
}
