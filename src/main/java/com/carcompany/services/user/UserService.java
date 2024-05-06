package com.carcompany.services.user;

import com.carcompany.components.JwtTokenUtil;
import com.carcompany.dtos.user.*;
import com.carcompany.exceptions.*;
import com.carcompany.models.*;
import com.carcompany.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserImageRepository imageRepository;
    private final LicenseRepository licenseRepository;
    private final UserLicenseRepository userLicenseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        // Register User
        String phoneNumber = userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException("You cannot register an admin account");
        }

        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .role(role)
                .status(userDTO.getStatus())
                .build();

        String password = userDTO.getPassword();
        String encodePassword = passwordEncoder.encode(password);
        newUser.setPassword(encodePassword);

        return userRepository.save(newUser);
    }


    @Override
    public User getUserById(Long id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id =" + id));
    }

    @Override
    public UserImage createUserImage(Long userId, UserImageDTO userImageDTO) throws Exception {
        User existingUser = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find user with id: "+ userImageDTO.getUserId()));
        UserImage newUserImage = UserImage.builder()
                .user(existingUser)
                .imageUrl(userImageDTO.getImageUrl())
                .build();
        //Ko cho insert quá 5 ảnh cho 1 sản phẩm
        int size = imageRepository.findByUserId(userId).size();
        if(size >= UserImage.MAXIMUM_IMAGES_PER_USER) {
            throw new InvalidParamException(
                    "Number of images must be <= "
                            + UserImage.MAXIMUM_IMAGES_PER_USER);
        }
        if(existingUser.getImg() == null){
            existingUser.setImg(newUserImage.getImageUrl());
        }
        userRepository.save(existingUser);
        return imageRepository.save(newUserImage);
    }

    @Override
    public UserLicense createUserLicense(Long userId, UserLicenseDTO licenseDTO) throws Exception {
        User existingUser = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find user with id: "+ licenseDTO.getUserId()));

        UserLicense newUserLicense = UserLicense.builder()
                .user(existingUser)
                .licensesId(licenseDTO.getLicenseId())
                .build();
        userRepository.save(existingUser);
        return userLicenseRepository.save(newUserLicense);
    }

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) {
        return userRepository.findAll(keyword, pageable);
    }

    @Override
    public Page<User> findUsersByRoleId(long roleId, Pageable pageable) {
        return userRepository.findByRoleId(roleId, pageable);
    }

    @Override
    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        return getUserDetailsFromToken(existingToken.getToken());
    }


    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
        // Find the existing user by userId
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Update user information based on the DTO
        if (updatedUserDTO.getFullName() != null) {
            existingUser.setFullName(updatedUserDTO.getFullName());
        }
//        if (newPhoneNumber != null) {
//            existingUser.setPhoneNumber(newPhoneNumber);
//        }
        if (updatedUserDTO.getAddress() != null) {
            existingUser.setAddress(updatedUserDTO.getAddress());
        }

        if (updatedUserDTO.getStatus() != null) {
            existingUser.setStatus(updatedUserDTO.getStatus());
        }

        //existingUser.setRole(updatedRole);
        // Save the updated user
        return userRepository.save(existingUser);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) throws Exception {
        Optional<User> optionalUser = Optional.empty();
        String subject = null;
        // Check if the user exists by phone number
        if (userLoginDTO.getPhoneNumber() != null && !userLoginDTO.getPhoneNumber().isBlank()) {
            optionalUser = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber());
            subject = userLoginDTO.getPhoneNumber();
        }

        // If user is not found, throw an exception
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Not found");
        }

        // Get the existing user
        User existingUser = optionalUser.get();

        //check password

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), existingUser.getPassword())) {
            throw new BadCredentialsException("WRONG_PHONE_PASSWORD");

        }

        Optional<Role> optionalRole = roleRepository.findById(userLoginDTO.getRoleId());
        if(optionalRole.isEmpty() || !userLoginDTO.getRoleId().equals(existingUser.getRole().getId())) {
            throw new DataNotFoundException("ROLE_DOES_NOT_EXISTS");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                subject, userLoginDTO.getPassword(),
                existingUser.getAuthorities()
        );

        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)) {
            throw new ExpiredTokenException("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear token
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        for (Token token : tokens) {
            tokenRepository.delete(token);
        }
    }

    @Override
    public Page<User> findByLicenseAndStatus(Long licenseId, String status, Pageable pageable) {
        return userRepository.findByLicenseIdAndStatus(licenseId, status, pageable);
    }

    @Override
    @Transactional
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void changeProfileImage(Long userId, String imageName) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        existingUser.setImg(imageName);
        userRepository.save(existingUser);
    }
}
