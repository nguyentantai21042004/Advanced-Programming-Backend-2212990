package com.carcompany.dtos.maintenance;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleMaintenanceHistoryDTO {
    @JsonProperty("vehicle_id")
    @Min(value = 1, message = "Product's ID must be > 0")
    private Long vehicleId;

    @JsonProperty("maintenance_date")
    private Date maintenanceDate;

    @JsonProperty("maintenance_type")
    private String maintenanceType;

    @JsonProperty("description")
    private String description;


    @JsonProperty("cost")
    private double cost;
}
