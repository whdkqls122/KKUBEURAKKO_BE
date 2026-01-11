package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;

@Repository
public interface StoreRepository extends JpaRepository<Stores, Long> {
	@Query("""
			select s from Stores s
			where s.isOpen = false
			and s.pauseEndAt <= :now
		""")
	List<Stores> findPausedStoresToReopen(LocalDateTime now);
}
