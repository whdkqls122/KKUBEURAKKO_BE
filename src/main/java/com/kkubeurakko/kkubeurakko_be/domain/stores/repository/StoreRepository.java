package com.kkubeurakko.kkubeurakko_be.domain.stores.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.StoreStatus;
import com.kkubeurakko.kkubeurakko_be.domain.stores.entity.Stores;

@Repository
public interface StoreRepository extends JpaRepository<Stores, Long> {

	@Query("""
		    select s
		    from Stores s
		    join s.storeBusinessHoursList h
		    where s.status in :statuses
		""")
	List<Stores> findStoresByStatuses(List<StoreStatus> statuses);

	@Query("""
		    select s from Stores s
		    where s.status = :status
		""")
	List<Stores> findClosedStores(StoreStatus status);
}
