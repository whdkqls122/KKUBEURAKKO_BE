package com.kkubeurakko.kkubeurakko_be.domain.coupons.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;

import jakarta.persistence.LockModeType;

@Repository
public interface CouponRepository extends JpaRepository<Coupons, Long> {
	@Query("""
		    SELECT c FROM Coupons c
		    WHERE c.id NOT IN (
		        SELECT uc.coupon.id
		        FROM UsersCoupons uc
		        WHERE uc.user.userNumber = :userNumber
		    )
		    AND c.validUntil >= CURRENT_DATE
		""")
	List<Coupons> findAllUserDoesNotHave(@Param("userNumber") String userNumber);

	@Query("""
		SELECT uc.coupon
		FROM UsersCoupons uc
		WHERE uc.user = :userNumber
		AND uc.coupon.validUntil >= CURRENT_DATE
		AND uc.isUsed = false
		""")
	List<Coupons> findAllUserOwned(@Param("userNumber") String userNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM Coupons c WHERE c.id = :couponId")
	Optional<Coupons> findByIdForUpdate(@Param("couponId") Long couponId);
}
