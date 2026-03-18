package com.kkubeurakko.kkubeurakko_be.domain.orders.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Orders;
import com.kkubeurakko.kkubeurakko_be.domain.orders.entity.Status;
import com.kkubeurakko.kkubeurakko_be.domain.users.entity.Users;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
	List<Orders> findByUser(Users user);

	@Query("""
		    SELECT DISTINCT o FROM Orders o
		    join fetch o.orderItemList oi
		    WHERE o.id = :orderId AND o.user = :user
		""")
	Optional<Orders> findByIdAndUser(Long orderId, Users user);

	@Query("""
		    select distinct o
		    from Orders o
		    join o.orderItemList oi
		    join oi.menu m
		    join m.menuGroup mg
		    join mg.store s
		    where s.id = :storeId
		    and o.status = :status
		    and o.orderDate between :start and :end
		    order by o.orderDate asc
		""")
	List<Orders> findByStoreIdAndStatusAndBusinessTime(
		Long storeId,
		Status status,
		LocalDateTime start,
		LocalDateTime end
	);

	boolean existsByUser_UserNumberAndStatusIn(String userNumber, List<Status> status);
}
