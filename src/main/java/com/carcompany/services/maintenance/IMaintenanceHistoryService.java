package com.carcompany.services.maintenance;

import com.carcompany.dtos.maintenance.UpdateMaintenancyDTO;
import com.carcompany.dtos.maintenance.VehicleMaintenanceHistoryDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.VehicleMaintenanceHistory;
import com.carcompany.responses.maintenance.MaintenanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IMaintenanceHistoryService {
    Page<MaintenanceResponse> getAllMaintenances(PageRequest pageRequest);

    Page<MaintenanceResponse> getMaintenanceByVehicle(Long id, PageRequest pageRequest) throws DataNotFoundException;

    VehicleMaintenanceHistory findById(Long id) throws DataNotFoundException;
    VehicleMaintenanceHistory createMaintenance(VehicleMaintenanceHistoryDTO maintenanceHistoryDTO) throws Exception;

    void createMaintenanceImage(Long vehicleId, String fileName) throws Exception;

    void updateMaintenance(Long id, UpdateMaintenancyDTO updateMaintenancyDTO) throws Exception;

    public void deleteMaintenance(Long id) throws Exception;
}
