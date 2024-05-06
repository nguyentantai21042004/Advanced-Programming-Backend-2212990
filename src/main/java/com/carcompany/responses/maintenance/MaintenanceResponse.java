package com.carcompany.responses.maintenance;

import com.carcompany.models.VehicleMaintenanceHistory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceResponse {
    private Long id;

    @JsonProperty("maintenance_date")
    private Date maintenanceDate;

    @JsonProperty("maintenance_type")
    private String maintenanceType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("cost")
    private double cost;

    public static MaintenanceResponse fromVehicleMaintenanceHistory(VehicleMaintenanceHistory vehicleMaintenanceHistory) {
        return MaintenanceResponse.builder()
                .id(vehicleMaintenanceHistory.getId())
                .maintenanceDate(vehicleMaintenanceHistory.getMaintenanceDate())
                .maintenanceType(vehicleMaintenanceHistory.getMaintenanceType())
                .description(vehicleMaintenanceHistory.getDescription())
                .status(vehicleMaintenanceHistory.getStatus())
                .cost(vehicleMaintenanceHistory.getCost())
                .build();
    }
}
