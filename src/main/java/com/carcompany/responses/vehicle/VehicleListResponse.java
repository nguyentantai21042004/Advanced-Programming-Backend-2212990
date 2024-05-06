package com.carcompany.responses.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class VehicleListResponse {
    private List<VehicleResponse> vehicles;
    private int totalPages;
}
