package com.kkubeurakko.kkubeurakko_be.domain.coupons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;

@Repository
public interface UserCouponRepository extends JpaRepository<UsersCoupons, Long> {
	boolean existsByUserUserNumberAndCouponId(String userNumber, Long couponId);
}
