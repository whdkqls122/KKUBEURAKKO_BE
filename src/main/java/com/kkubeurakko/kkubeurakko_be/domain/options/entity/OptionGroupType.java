package com.kkubeurakko.kkubeurakko_be.domain.options.entity;

import java.util.List;

public enum OptionGroupType {
	SINGLE {
		@Override
		public int calculateSelectedNum(int input, List<Options> options) {
			return 1;
		}
	},
	MULTI {
		@Override
		public int calculateSelectedNum(int input, List<Options> options) {
			return input;
		}
	},
	ALL {
		@Override
		public int calculateSelectedNum(int input, List<Options> options) {
			return options.size();
		}
	};

	public abstract int calculateSelectedNum(int input, List<Options> options);
}
