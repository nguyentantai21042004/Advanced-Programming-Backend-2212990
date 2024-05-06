package com.carcompany.services.vehicle;

import com.carcompany.dtos.vehicle.VehicleDTO;
import com.carcompany.dtos.vehicle.VehicleImageDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.exceptions.InvalidParamException;
import com.carcompany.exceptions.ValidParamException;
import com.carcompany.models.License;
import com.carcompany.models.Vehicle;
import com.carcompany.models.VehicleImage;
import com.carcompany.repositories.LicenseRepository;
import com.carcompany.repositories.VehicleImageRepository;
import com.carcompany.repositories.VehicleRepository;
import com.carcompany.responses.vehicle.VehicleResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService implements IVehicleService{
    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository imageRepository;
    private final LicenseRepository licenseRepository;

    @Override
    public Vehicle insertVehicle(VehicleDTO vehicleDTO) throws Exception {
        Vehicle existingVehicle = vehicleRepository.findByLicensePlate(vehicleDTO.getLicense_plate());

        if (existingVehicle != null) {
            throw new ValidParamException("Already have the vehicle with this license plate: " + vehicleDTO.getLicense_plate());
        }

        Optional<License> licenseOptional = licenseRepository.findById(vehicleDTO.getDriver_license());
        License license = licenseOptional.orElseThrow(ChangeSetPersister.NotFoundException::new);

        Vehicle newVehicle = Vehicle.builder()
                .name(vehicleDTO.getName())
                .payload(vehicleDTO.getPayload())
                .material(vehicleDTO.getMaterial())
                .status(vehicleDTO.getStatus())
                .licensePlate(vehicleDTO.getLicense_plate())
                .vehicleType(vehicleDTO.getVehicle_type())
                .driverLicense(license.getId())
                .rentalPrice(vehicleDTO.getRental_price())
                .build();

        return vehicleRepository.save(newVehicle);
    }

    @Override
    public Vehicle getVehicleById(long id) throws Exception {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find vehicle with id =" + id));
    }

    @Override
    public Vehicle updateVehicle(Long id, VehicleDTO vehicleDTO) throws DataNotFoundException {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find a vehicle with id: " + id));

        if (vehicleDTO.getName() != null) {
            existingVehicle.setName(vehicleDTO.getName());
        }
        if (vehicleDTO.getPayload() != 0) {
            existingVehicle.setPayload(vehicleDTO.getPayload());
        }
        if (vehicleDTO.getMaterial() != null) {
            existingVehicle.setMaterial(vehicleDTO.getMaterial());
        }
        if (vehicleDTO.getStatus() != null) {
            existingVehicle.setStatus(vehicleDTO.getStatus());
        }
        if (vehicleDTO.getLicense_plate() != null) {
            existingVehicle.setLicensePlate(vehicleDTO.getLicense_plate());
        }
        if (vehicleDTO.getVehicle_type() != null) {
            existingVehicle.setVehicleType(vehicleDTO.getVehicle_type());
        }

        if (vehicleDTO.getRental_price() != 0) {
            existingVehicle.setRentalPrice(vehicleDTO.getRental_price());
        }

        return vehicleRepository.save(existingVehicle);
    }

    @Override
    public Page<VehicleResponse> getAllVehicles(String keyword, String vehicleType, PageRequest pageRequest) {
        Page<Vehicle> vehiclePage = vehicleRepository.searchVehicles(keyword, vehicleType, pageRequest);

        return vehiclePage.map(VehicleResponse::fromVehicle);
    }

    @Override
    public List<Vehicle> getVehiclesByType(String vehicleType) {
        return vehicleRepository.searchVehiclesByType(vehicleType);
    }

    @Override
    public VehicleImage createVehicleImage(Long vehicleId, VehicleImageDTO vehicleImageDTO) throws Exception {
        Vehicle existingVehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find vehicle with id: "+ vehicleImageDTO.getProductId()));
        VehicleImage newProductImage = VehicleImage.builder()
                .vehicle(existingVehicle)
                .imageUrl(vehicleImageDTO.getImageUrl())
                .build();
        //Ko cho insert quá 5 ảnh cho 1 sản phẩm
        int size = imageRepository.findByVehicleId(vehicleId).size();
        if(size >= VehicleImage.MAXIMUM_IMAGES_PER_VEHICLE) {
            throw new InvalidParamException(
                    "Number of images must be <= "
                            + VehicleImage.MAXIMUM_IMAGES_PER_VEHICLE);
        }
        if (existingVehicle.getThumbnail() == null ) {
            existingVehicle.setThumbnail(newProductImage.getImageUrl());
        }
        vehicleRepository.save(existingVehicle);
        return imageRepository.save(newProductImage);
    }

    @Override
    @Transactional
    public void deleteProduct(long id) {
        Optional<Vehicle> optionalProduct = vehicleRepository.findById(id);
        optionalProduct.ifPresent(vehicleRepository::delete);
    }
}
