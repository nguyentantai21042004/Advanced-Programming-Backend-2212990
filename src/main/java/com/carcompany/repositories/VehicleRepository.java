package com.carcompany.repositories;

import com.carcompany.models.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Vehicle findByLicensePlate(String licensePlate);

    List<Vehicle> findByVehicleType(String vehicleType);

    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:vehicleType IS NULL OR :vehicleType = '' OR v.vehicleType = :vehicleType) " +
            "AND (:keyword IS NULL OR :keyword = '' OR v.name LIKE %:keyword%)" )
    Page<Vehicle> searchVehicles(
            @Param("keyword") String keyword,
            @Param("vehicleType") String vehicleType, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE " + "(:vehicleType IS NULL OR :vehicleType = '' OR v.vehicleType = :vehicleType) ")
    List<Vehicle> searchVehiclesByType( @Param("vehicleType") String vehicleType);
}
