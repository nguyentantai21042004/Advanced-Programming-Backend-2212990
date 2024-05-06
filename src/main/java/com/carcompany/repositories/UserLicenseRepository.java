package com.carcompany.repositories;

import com.carcompany.models.UserLicense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLicenseRepository extends JpaRepository<UserLicense, Long> {
}
