package com.kkubeurakko.kkubeurakko_be.domain.addresses.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kkubeurakko.kkubeurakko_be.domain.addresses.entity.Addresses;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
	@Query("select a from Addresses a where a.user = :user order by a.isPrimary desc")
	List<Addresses> findAllByUser(@Param("user") Users user);

	int countByUser(Users user);

	Optional<Addresses> findByUserAndIsPrimary(Users user, boolean isPrimary);

	Optional<Addresses> findByIdAndUser(Long id, Users user);

	boolean existsByUser(Users user);

	Optional<Addresses> findFirstByUserOrderByIdDesc(Users user);
}
