package com.kkubeurakko.kkubeurakko_be.domain.coupons.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Coupons;
import com.kkubeurakko.kkubeurakko_be.domain.coupons.entity.Status;

import jakarta.persistence.LockModeType;

@Repository
public interface CouponRepository extends JpaRepository<Coupons, Long> {
	@Query("""
		    SELECT c
		    FROM Coupons c
		    LEFT JOIN UsersCoupons uc ON uc.coupon = c AND uc.user.userNumber = :userNumber
		    WHERE uc.id IS NULL
		    AND c.validUntil >= CURRENT_DATE
		    AND c.validFrom <= CURRENT_DATE
		    AND c.quantity > 0
		    AND c.status = :status
		""")
	List<Coupons> findAllUserDoesNotHave(@Param("userNumber") String userNumber, @Param("status") Status status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM Coupons c WHERE c.id = :couponId")
	Optional<Coupons> findByIdForUpdate(@Param("couponId") Long couponId);
}
