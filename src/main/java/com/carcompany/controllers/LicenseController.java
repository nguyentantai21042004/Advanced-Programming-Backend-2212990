package com.carcompany.controllers;

import com.carcompany.models.License;
import com.carcompany.responses.ResponseObject;
import com.carcompany.services.license.ILicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/licenses")
@RequiredArgsConstructor
public class LicenseController {
    private final ILicenseService licenseService;

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("") // http://localhost:8080/api/v1/licenses
    public ResponseEntity<ResponseObject> getAllLicenses(){
        List<License> licenses=  licenseService.getAllRoles();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Successfully get Licenses List")
                .data(licenses)
                .status(HttpStatus.OK)
                .build());
    }

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
}
