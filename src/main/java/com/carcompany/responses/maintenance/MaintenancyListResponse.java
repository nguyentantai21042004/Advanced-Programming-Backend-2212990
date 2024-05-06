package com.carcompany.responses.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MaintenancyListResponse {
    private List<MaintenanceResponse> maintenances;
    private int totalPages;
}
