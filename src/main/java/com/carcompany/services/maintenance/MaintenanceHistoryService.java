package com.carcompany.services.maintenance;


import com.carcompany.dtos.maintenance.UpdateMaintenancyDTO;
import com.carcompany.dtos.maintenance.VehicleMaintenanceHistoryDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.exceptions.ValidParamException;
import com.carcompany.models.Vehicle;
import com.carcompany.models.VehicleMaintenanceHistory;
import com.carcompany.repositories.VehicleMaintenanceHistoryRepository;
import com.carcompany.repositories.VehicleRepository;
import com.carcompany.responses.maintenance.MaintenanceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceHistoryService implements IMaintenanceHistoryService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMaintenanceHistoryRepository maintenanceHistoryRepository;

    @Override
    public Page<MaintenanceResponse> getAllMaintenances(PageRequest pageRequest) {
        Page<VehicleMaintenanceHistory> orderPage = maintenanceHistoryRepository.searchMaintenances(pageRequest);
        return orderPage.map(MaintenanceResponse::fromVehicleMaintenanceHistory);
    }

    @Override
    public Page<MaintenanceResponse> getMaintenanceByVehicle(Long id, PageRequest pageRequest) throws DataNotFoundException{
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find a vehicle with id = " + id));

        Page<VehicleMaintenanceHistory> orderPage = maintenanceHistoryRepository.searchMaintenancesByVehicleId(id, pageRequest);
        return orderPage.map(MaintenanceResponse::fromVehicleMaintenanceHistory);
    }

    @Override
    public VehicleMaintenanceHistory findById(Long id) throws DataNotFoundException{
        return maintenanceHistoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("No maintenance history found with ID" + id));
    }

    @Override
    @Transactional
    public VehicleMaintenanceHistory createMaintenance(VehicleMaintenanceHistoryDTO maintenanceHistoryDTO) throws ValidParamException {
        Vehicle existingVehicle = vehicleRepository.findById(maintenanceHistoryDTO.getVehicleId())
                .orElseThrow(() -> new ValidParamException("No vehicle found with ID: " + maintenanceHistoryDTO.getVehicleId()));

        if(!existingVehicle.getStatus().equals("AVAILABLE")){
            return null;
        }

        VehicleMaintenanceHistory newMaintenanceHistory = VehicleMaintenanceHistory.builder()
                .vehicle(existingVehicle)
                .maintenanceDate(maintenanceHistoryDTO.getMaintenanceDate())
                .maintenanceType(maintenanceHistoryDTO.getMaintenanceType())
                .description(maintenanceHistoryDTO.getDescription())
                .cost(maintenanceHistoryDTO.getCost())
                .build();

        existingVehicle.setStatus("MAINTENANCE");
        newMaintenanceHistory.setStatus("PROCESSING");

        vehicleRepository.save(existingVehicle);
        return maintenanceHistoryRepository.save(newMaintenanceHistory);
    }

    @Override
    @Transactional
    public void createMaintenanceImage(Long vehicleId, String fileName) throws Exception {
            VehicleMaintenanceHistory existingMaintenance = maintenanceHistoryRepository.findById(vehicleId)
                    .orElseThrow(() -> new DataNotFoundException("No maintenance history found with ID" + vehicleId));
            if(existingMaintenance.getImageUrl() == null){
                existingMaintenance.setImageUrl(fileName);
            }

            maintenanceHistoryRepository.save(existingMaintenance);
    }

    @Override
    @Transactional
    public void updateMaintenance(Long id, UpdateMaintenancyDTO updateMaintenancyDTO) throws Exception {
        VehicleMaintenanceHistory maintenanceOptional = maintenanceHistoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Can not find maintain history"));

        Vehicle existingVehicle = vehicleRepository.findById(maintenanceOptional.getVehicle().getId())
                .orElseThrow(() -> new DataNotFoundException("Can not find a vehicle with id = " + maintenanceOptional.getVehicle().getId()));

        if(updateMaintenancyDTO.getStatus().equals("DONE") || updateMaintenancyDTO.getStatus().equals("CANCELED")){
            maintenanceOptional.setStatus(updateMaintenancyDTO.getStatus());
            existingVehicle.setStatus("AVAILABLE");
        }

        vehicleRepository.save(existingVehicle);
        maintenanceHistoryRepository.save(maintenanceOptional);
    }

    @Override
    @Transactional
    public void deleteMaintenance(Long id) throws Exception {
        Optional<VehicleMaintenanceHistory> vehicleMaintenanceHistory = maintenanceHistoryRepository.findById(id);
        vehicleMaintenanceHistory.ifPresent(maintenanceHistoryRepository::delete);
    }
}
