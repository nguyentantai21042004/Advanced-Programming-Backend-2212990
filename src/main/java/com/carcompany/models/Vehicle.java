package com.carcompany.models;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "vehicle")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vehicle extends com.carcompany.models.BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 350)
    private String name;

    @Column(name = "payload", nullable = false, length = 350)
    private int payload;

    @Column(name = "material", nullable = false, length = 20)
    private String material;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType;

    @Column(name = "driver_license", nullable = false)
    private Long driverLicense; // Chuyển kiểu dữ liệu từ String sang Long

    @Column(name = "rental_price")
    @NotNull // Ensure rentalPrice is not null
    private Float rentalPrice;

    @Column(name = "thumbnail", length = 200)
    private String thumbnail;
}
