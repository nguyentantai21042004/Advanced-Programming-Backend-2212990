package com.carcompany.services.user;

import com.carcompany.dtos.user.*;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.exceptions.InvalidPasswordException;
import com.carcompany.models.User;
import com.carcompany.models.UserImage;
import com.carcompany.models.UserLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    User getUserById(Long id) throws Exception;

    UserImage createUserImage(
            Long userId,
            UserImageDTO userImageDTO
    ) throws Exception;

    UserLicense createUserLicense(
            Long userId,
            UserLicenseDTO licenseDTO
    ) throws Exception;
    Page<User> findAll(String keyword, Pageable pageable) throws Exception;
    Page<User> findUsersByRoleId(long roleId, Pageable pageable);

    User getUserDetailsFromRefreshToken(String token) throws Exception;


    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;
    String login(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;

    void resetPassword(Long userId, String newPassword)
            throws InvalidPasswordException, DataNotFoundException;

    Page<User> findByLicenseAndStatus(Long licenseId, String status, Pageable pageable);

    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
    public void changeProfileImage(Long userId, String imageName) throws Exception;
}
