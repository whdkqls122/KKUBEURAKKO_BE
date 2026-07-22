package com.kkubeurakko.kkubeurakko_be.domain.users.util;

public class PhoneMaskingUtil {
	public static String mask(String phone) {
		String digits = phone.replaceAll("\\D", "");

		if (digits.length() != 11) {
			return phone;
		}

		return digits.replaceAll(
			"(\\d{3})(\\d{2})\\d{2}(\\d{2})\\d{2}",
			"$1-$2**-$3**"
		);
	}
}
