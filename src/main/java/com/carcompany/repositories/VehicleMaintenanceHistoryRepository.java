package com.carcompany.repositories;

import com.carcompany.models.DeliveryOrder;
import com.carcompany.models.VehicleMaintenanceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleMaintenanceHistoryRepository extends JpaRepository<com.carcompany.models.VehicleMaintenanceHistory, Long> {
    @Query("SELECT v FROM VehicleMaintenanceHistory v")
    Page<VehicleMaintenanceHistory> searchMaintenances(Pageable pageable);

    @Query("SELECT v FROM VehicleMaintenanceHistory v WHERE v.vehicle.id = :vehicleId")
    Page<VehicleMaintenanceHistory> searchMaintenancesByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);

}
