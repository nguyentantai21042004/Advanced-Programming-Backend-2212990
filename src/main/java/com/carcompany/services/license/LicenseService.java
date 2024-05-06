package com.carcompany.services.license;

import com.carcompany.models.License;
import com.carcompany.repositories.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LicenseService implements ILicenseService{
    private final LicenseRepository licenseRepository;
    @Override
    public List<License> getAllRoles() {
        return licenseRepository.findAll();
    }

    @Override
    public Long findLicenseIdByName(String name) {
        Optional<License> licenseOptional = licenseRepository.findByName(name);
        return licenseOptional.map(License::getId).orElse(null);
    }
}
