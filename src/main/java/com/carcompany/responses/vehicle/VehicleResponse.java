package com.carcompany.responses.vehicle;

import com.carcompany.models.Vehicle;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponse {
    private Long id;
    private String name;
    private int payload;
    private String material;
    private String status;
    private String licensePlate;
    private String vehicleType;
    private Long driverLicense;
    private String thumbnail;
    private float rentalPrice;

    public static VehicleResponse fromVehicle(Vehicle vehicle) {
        return VehicleResponse.builder()
                    .id(vehicle.getId())
                    .name(vehicle.getName())
                    .material(vehicle.getMaterial())
                    .payload(vehicle.getPayload())
                    .status(vehicle.getStatus())
                    .licensePlate(vehicle.getLicensePlate())
                    .vehicleType(vehicle.getVehicleType())
                    .driverLicense(vehicle.getDriverLicense())
                    .thumbnail(vehicle.getThumbnail())
                    .rentalPrice(vehicle.getRentalPrice())
                    .build();
    }
}
