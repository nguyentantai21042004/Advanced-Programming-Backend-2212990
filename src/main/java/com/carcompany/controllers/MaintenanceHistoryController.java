package com.carcompany.controllers;

import com.carcompany.dtos.maintenance.UpdateMaintenancyDTO;
import com.carcompany.dtos.maintenance.VehicleMaintenanceHistoryDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.VehicleMaintenanceHistory;
import com.carcompany.responses.maintenance.MaintenanceResponse;
import com.carcompany.responses.maintenance.MaintenancyListResponse;
import com.carcompany.responses.ResponseObject;
import com.carcompany.services.maintenance.IMaintenanceHistoryService;
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

@RestController
@RequestMapping("${api.prefix}/maintenances")
@RequiredArgsConstructor
public class MaintenanceHistoryController {
    private final IMaintenanceHistoryService maintenanceHistoryService;
    private final IVehicleService vehicleService;

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("") // http://localhost:8080/api/v1/maintenances
    public ResponseEntity<ResponseObject> getAllMaintenances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int limit
    ){
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<MaintenanceResponse> orderPage = maintenanceHistoryService.getAllMaintenances(pageRequest);

        MaintenancyListResponse response = MaintenancyListResponse.builder()
                .maintenances(orderPage.getContent())
                .totalPages(orderPage.getTotalPages())
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Get All Maintenances Success")
                .status(HttpStatus.OK)
                .data(response)
                .build());
    }

    @GetMapping("/{id}") // http://localhost:8080/api/v1/maintenances/1
    public ResponseEntity<ResponseObject> getMaintenanceByVehicle(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int limit
    ) throws DataNotFoundException {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").descending());
        Page<MaintenanceResponse> orderPage = maintenanceHistoryService.getMaintenanceByVehicle(id, pageRequest);

        MaintenancyListResponse response = MaintenancyListResponse.builder()
                .maintenances(orderPage.getContent())
                .totalPages(orderPage.getTotalPages())
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Successfully Get All Maintenances With ID = " + id)
                .status(HttpStatus.OK)
                .data(response)
                .build());
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("imgs/maintenanceUploads/" + imageName);
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
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/


    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @PostMapping("") // http://localhost:8080/api/v1/maintenances
    public ResponseEntity<ResponseObject> insertMaintenance(@Valid @RequestBody VehicleMaintenanceHistoryDTO vehicleMaintenanceHistoryDTO,
                                           BindingResult result) throws Exception {
        if(result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Something wrong")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(errorMessages)
                    .build());
        }
        VehicleMaintenanceHistory newMaintenanceHistory = maintenanceHistoryService.createMaintenance(vehicleMaintenanceHistoryDTO);
        if(newMaintenanceHistory == null){
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("This vehicle is not AVAILABLE")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Successfully Insert A New Maintenance")
                .status(HttpStatus.OK)
                .data(newMaintenanceHistory)
                .build());
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> uploadImage(
            @PathVariable("id") Long maintenanceId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            VehicleMaintenanceHistory existingMaintenance = maintenanceHistoryService.findById(maintenanceId);
            files = files == null ? new ArrayList<MultipartFile>() : files;

            if(files.size() > VehicleMaintenanceHistory.MAXIMUM_IMAGES_PER_MAINTENANCE){
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .message("You can only upload maximum 1 image")
                        .status(HttpStatus.BAD_REQUEST)
                        .build());
            }

            for(MultipartFile file : files){
                if(file.getSize() == 0)
                    continue;

                if(file.getSize() > 10 * 1024 * 1024){ // > 10MB
                    return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .message("File is too large ! Maximum size is 10MB")
                            .status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .build());
                }

                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .message("File must be an image")
                            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .build());
                }

                String filename = storeFile(file);

                maintenanceHistoryService.createMaintenanceImage(maintenanceId, filename);
            }
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Successfully Insert A Image")
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Something wrong")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(e.getMessage())
                    .build());
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

        Path uploadDir = Paths.get("imgs/maintenanceUploads");

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


    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateMaintenance(@PathVariable Long id, @RequestBody UpdateMaintenancyDTO updateMaintenancyDTO) {
        try {
            maintenanceHistoryService.updateMaintenance(id, updateMaintenancyDTO);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Successfully Update A Maintenance")
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Something wrong")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(e.getMessage())
                    .build());
        }
    }

    // Delete a maintenance record
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteVehicle(@PathVariable Long id) {
        try {
            maintenanceHistoryService.deleteMaintenance(id);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Successfully Delete A Maintenance")
                    .status(HttpStatus.OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Something wrong")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(e.getMessage())
                    .build());
        }
    }
}
