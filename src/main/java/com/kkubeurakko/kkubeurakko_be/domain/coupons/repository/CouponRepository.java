package com.kkubeurakko.kkubeurakko_be.domain.coupons.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface CouponRepository extends JpaRepository<Coupons, Long> {
	@Query("""
			select c from Coupons c
			where c.id not in (
				select uc.coupon.id
				from UsersCoupons uc
				where uc.user = :user
			)
		""")
	List<Coupons> findAllUserDoesNotHave(@Param("user") Users user);
}
