package com.carcompany.controllers;

import com.carcompany.dtos.vehicle.VehicleDTO;
import com.carcompany.dtos.vehicle.VehicleImageDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.Vehicle;
import com.carcompany.models.VehicleImage;
import com.carcompany.responses.ResponseObject;
import com.carcompany.responses.vehicle.VehicleListResponse;
import com.carcompany.responses.vehicle.VehicleResponse;
import com.carcompany.services.vehicle.IVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final IVehicleService vehicleService;

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllVehicles(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "", name = "vehicle_type") String vehicleType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<VehicleResponse> vehiclePage = vehicleService.getAllVehicles(keyword, vehicleType, pageRequest);

        VehicleListResponse response = VehicleListResponse.builder()
                .vehicles(vehiclePage.getContent())
                .totalPages(vehiclePage.getTotalPages())
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get all vehicles successfully")
                        .status(HttpStatus.OK)
                        .data(response)
                        .build());
    }

    @GetMapping("/type")
    public ResponseEntity<ResponseObject> getVehiclesByType(
            @RequestParam(defaultValue = "", name = "vehicle_type") String vehicleType
    ) {
        if (vehicleType == null || vehicleType.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Please provide a valid vehicle type")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }

        List<Vehicle> vehicles = vehicleService.getVehiclesByType(vehicleType);

        List<VehicleResponse> vehicleResponses = vehicles.stream()
                .map(VehicleResponse::fromVehicle)
                .collect(Collectors.toList());

        VehicleListResponse response = VehicleListResponse.builder()
                .vehicles(vehicleResponses)
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Successfully retrieved vehicles of type: " + vehicleType)
                .status(HttpStatus.OK)
                .data(response)
                .build());
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("imgs/vehicleUploads/" + imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()));
                //return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> findVehicleById(@PathVariable Long id) {
        try {
            Vehicle existingVehicle = vehicleService.getVehicleById(id);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Successfully get vehicle with id = " + id)
                    .data(VehicleResponse.fromVehicle(existingVehicle))
                    .status(HttpStatus.OK)
                    .build());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Some thing wrong")
                    .data(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/    
    @PostMapping("") // http://localhost:8080/api/v1/vehicles
    public ResponseEntity<ResponseObject> insertVehicle(
            @Valid @RequestBody VehicleDTO vehicleDTO,
            BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                .message("Some thing wrong in your input")
                                .data(errorMessages)
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
            Vehicle newVehicle = vehicleService.insertVehicle(vehicleDTO);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Insert new vehicle successfully")
                    .data(VehicleResponse.fromVehicle(newVehicle))
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Some thing wrong in your input")
                    .data(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable("id") Long vehicleId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Vehicle existingVehicle = vehicleService.getVehicleById(vehicleId);
            files = files == null ? new ArrayList<MultipartFile>() : files;

            if(files.size() > VehicleImage.MAXIMUM_IMAGES_PER_VEHICLE){
                return ResponseEntity.badRequest().body("You can only upload maximum 5 images");
            }

            List<VehicleImage> vehicleImages = new ArrayList<>();

            for(MultipartFile file : files){
                if(file.getSize() == 0)
                    continue;

                if(file.getSize() > 10 * 1024 * 1024){ // > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large ! Maximum size is 10MB");
                }

                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image");
                }

                String filename = storeFile(file);
                VehicleImage vehicleImage =  vehicleService.createVehicleImage(
                        existingVehicle.getId(),
                        VehicleImageDTO.builder()
                                .imageUrl(filename)
                                .build());
                vehicleImages.add(vehicleImage);
            }
            return ResponseEntity.ok(vehicleImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}") // http://localhost:8080/api/v1/vehicles/2
    public ResponseEntity<ResponseObject> updateVehicle(
        @Valid @PathVariable("id") Long id,
        @RequestBody VehicleDTO newVehicleDTO
    ) throws DataNotFoundException {
        try {
            Vehicle vehicle = vehicleService.updateVehicle(id, newVehicleDTO);

            return ResponseEntity.ok().body(ResponseObject.builder()
                            .message("Update a vehicle successfully")
                            .data(VehicleResponse.fromVehicle(vehicle))
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Something wrong in your input")
                    .data(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @DeleteMapping("/{id}") // http://localhost:8080/api/v1/vehicles/4
    public ResponseEntity<ResponseObject> deleteVehicle(@PathVariable Long id){
        try {
            vehicleService.deleteProduct(id);
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Delete this vehicle successfully")
                            .status(HttpStatus.OK)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Something wrong")
                            .data(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build()
            );
        }
    }

    private boolean isImageFile(MultipartFile file){
        String contentTypeType = file.getContentType();
        return contentTypeType != null && contentTypeType.startsWith("image/");
    }

    private String storeFile(MultipartFile file) throws IOException {
        if(!isImageFile(file) || file.getOriginalFilename() == null){
            throw  new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        // Add UUID ==> be the only
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;

        Path uploadDir = Paths.get("imgs/vehicleUploads");

        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }

        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
}
