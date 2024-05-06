package com.carcompany.dtos.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data // toString
@Getter
@Setter
@AllArgsConstructor
public class VehicleDTO {
    @NotEmpty(message = "Vehicle must have it own name")
    String name;

    int payload;

    String material;

    String status;

    @NotEmpty(message = "Vehicle must have it own license plate")
    String license_plate;

    @NotEmpty(message = "Vehicle must have it type")
    String vehicle_type;

    int driver_license; // Change the type to int for license ID

    float rental_price;
}
