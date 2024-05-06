package com.carcompany.services.license;

import com.carcompany.models.License;

import java.util.List;

public interface ILicenseService {
    List<License> getAllRoles();
    public Long findLicenseIdByName(String name);
}
