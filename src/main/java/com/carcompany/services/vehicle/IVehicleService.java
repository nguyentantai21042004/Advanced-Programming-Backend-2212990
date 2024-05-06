package com.carcompany.services.vehicle;

import com.carcompany.dtos.vehicle.VehicleDTO;
import com.carcompany.dtos.vehicle.VehicleImageDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.Vehicle;
import com.carcompany.models.VehicleImage;
import com.carcompany.responses.vehicle.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IVehicleService {
    public Vehicle insertVehicle(VehicleDTO vehicleDTO) throws Exception;
    public Vehicle getVehicleById(long id) throws Exception;
    public Vehicle updateVehicle(Long id, VehicleDTO vehicleDTO) throws DataNotFoundException;
    public Page<VehicleResponse> getAllVehicles(String keyword, String vehicleType, PageRequest pageRequest);
    List<Vehicle> getVehiclesByType(String vehicleType);
    public VehicleImage createVehicleImage(
            Long productId,
            VehicleImageDTO vehicleImageDTO
    ) throws Exception;

    void deleteProduct(long id);
}
