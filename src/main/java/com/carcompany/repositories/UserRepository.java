package com.carcompany.repositories;

import com.carcompany.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);
    //SELECT * FROM users WHERE phoneNumber=?Number=?

    Page<User> findByRoleId(Long roleId, Pageable pageable);

    @Query("SELECT o FROM User o WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.phoneNumber LIKE %:keyword%) " +
            "AND LOWER(o.role.name) = 'user' " +
            "AND o.status = :status")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u JOIN UserLicense ul ON u.id = ul.user.id JOIN License l ON ul.licensesId = l.id WHERE l.id = :licenseId AND u.status = :status")
    Page<User> findByLicenseIdAndStatus(Long licenseId, String status, Pageable pageable);
}
