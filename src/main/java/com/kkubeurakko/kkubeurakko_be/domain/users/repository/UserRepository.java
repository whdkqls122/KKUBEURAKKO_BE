package com.kkubeurakko.kkubeurakko_be.domain.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
	Optional<Users> findByUserNumber(String userNumber);

	boolean existsByUserNumber(String userNumber);

	boolean existsByPhoneHash(String phoneHash);

	void deleteByUserNumber(String userNumber);

	boolean existsById(String id);

	Optional<Users> findById(String id);

	Optional<Users> findByPhoneHash(String phoneHash);

	@Query("""
		    SELECT u
		    FROM Users u
		    WHERE NOT EXISTS (
		        SELECT uc.id
		        FROM UsersCoupons uc
		        WHERE uc.user = u
		        AND uc.coupon.id = :couponId
		    )
		""")
	List<Users> findUsersWithoutCoupon(Long couponId);
}
