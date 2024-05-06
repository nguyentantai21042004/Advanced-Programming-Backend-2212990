package com.carcompany.repositories;

import com.carcompany.models.License;
import com.carcompany.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByName(String name);

    Optional<License> findById(int id); // Thêm phương thức tìm kiếm bằng ID
}
