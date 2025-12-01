package com.kkubeurakko.kkubeurakko_be.domain.coupons.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.UsersCoupons;

@Repository
public interface UserCouponRepository extends JpaRepository<UsersCoupons, Long> {
	@Query("""
			SELECT uc
			FROM UsersCoupons uc
			WHERE uc.id = :userCouponId
			AND uc.user.userNumber = :userNumber
		""")
	Optional<UsersCoupons> findByIdAndUser(
		@Param("userCouponId") Long userCouponId,
		@Param("userNumber") String userNumber
	);

	boolean existsByUserUserNumberAndCouponId(String userNumber, Long couponId);
}
