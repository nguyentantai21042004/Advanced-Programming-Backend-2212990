package com.carcompany.repositories;

import com.carcompany.models.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    List<DeliveryOrder> findByUserId(Long userId);

    @Query("SELECT v FROM DeliveryOrder v")
    Page<DeliveryOrder> searchOrders(Pageable pageable);
}
