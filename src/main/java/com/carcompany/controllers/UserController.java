package com.carcompany.controllers;

import com.carcompany.dtos.user.*;
import com.carcompany.responses.*;
import com.carcompany.components.LocalizationUtils;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.exceptions.InvalidPasswordException;
import com.carcompany.models.Token;
import com.carcompany.models.User;
import com.carcompany.models.UserImage;
import com.carcompany.models.UserLicense;
import com.carcompany.responses.user.DriverResponse;
import com.carcompany.responses.user.LoginResponse;
import com.carcompany.responses.user.UserListResponse;
import com.carcompany.responses.user.UserResponse;
import com.carcompany.services.user.IUserService;
import com.carcompany.services.token.ITokenService;
import com.carcompany.components.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;
    private final SecurityUtils securityUtils;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception{
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );
        Page<UserResponse> userPage = userService.findAll(keyword, pageRequest)
                .map(UserResponse::fromUser);

        // Lấy tổng số trang
        int totalPages = userPage.getTotalPages();
        List<UserResponse> userResponses = userPage.getContent();
        UserListResponse userListResponse = UserListResponse
                .builder()
                .users(userResponses)
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Get user list successfully")
                .status(HttpStatus.OK)
                .data(userListResponse)
                .build());
    }

    @GetMapping("/drivers")
    public ResponseEntity<ResponseObject> getUsersByRoleId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int limit
    ) {
        try {
            int roleId = 2;
            // Tạo Pageable từ thông tin trang và giới hạn
                PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );

            // Gọi userService để lấy danh sách người dùng theo giấy phép và trạng thái
            Page<UserResponse> userPage = userService.findUsersByRoleId(roleId, pageRequest)
                    .map(UserResponse::fromUser);

            // Lấy thông tin về tổng số trang và danh sách người dùng trên trang hiện tại
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();

            // Tạo đối tượng UserListResponse từ danh sách người dùng và tổng số trang
            UserListResponse userListResponse = UserListResponse.builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build();

            // Trả về ResponseEntity với danh sách người dùng và mã HTTP 200 OK
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Get Drivers successfully")
                    .status(HttpStatus.OK)
                    .data(userListResponse)
                    .build());
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Error occurred while fetching users by roleId")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<ResponseObject> getDriverById(@PathVariable Long id) {
        try {
            User driver = userService.getUserById(id);
            DriverResponse response = DriverResponse.fromUser(driver);

            // Trả về ResponseEntity với danh sách người dùng và mã HTTP 200 OK
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Get Drivers successfully")
                    .status(HttpStatus.OK)
                    .data(response)
                    .build());
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Error occurred while fetching users by roleId")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/licensesId")
    public ResponseEntity<ResponseObject> getUsersByLicenseAndStatus(
            @RequestParam(defaultValue = "1") Long licenseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            // Tạo Pageable từ thông tin trang và giới hạn
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );

            // Gọi userService để lấy danh sách người dùng theo giấy phép và trạng thái
            Page<UserResponse> userPage = userService.findByLicenseAndStatus(licenseId, "AVAILABLE", pageRequest)
                    .map(UserResponse::fromUser);

            // Lấy thông tin về tổng số trang và danh sách người dùng trên trang hiện tại
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();

            // Tạo đối tượng UserListResponse từ danh sách người dùng và tổng số trang
            UserListResponse userListResponse = UserListResponse.builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build();

            // Trả về ResponseEntity với danh sách người dùng và mã HTTP 200 OK
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Get users by license and status successfully")
                    .status(HttpStatus.OK)
                    .data(userListResponse)
                    .build());
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("Error occurred while fetching users by license and status")
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .build());
        }
    }



    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result) throws Exception{
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .message(errorMessages.toString())
                    .build());
        }

        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            //registerResponse.setMessage();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .message("PASSWORD_NOT_MATCH")
                    .build());
        }
        User user = userService.createUser(userDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(UserResponse.fromUser(user))
                .message("Đăng ký tài khoản thành công")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) throws Exception {
        /// Kiểm tra thông tin đăng nhập và sinh token
        String token = userService.login(userLoginDTO);
        User userDetail = userService.getUserDetailsFromToken(token);

        LoginResponse loginResponse = LoginResponse.builder()
                .message("LOGIN_SUCCESSFULLY")
                .token(token)
                .username(userDetail.getUsername())
                .roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .id(userDetail.getId())
                .build();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Login successfully")
                .data(loginResponse)
                .status(HttpStatus.OK)
                .build());
    }
    @PostMapping("/refreshToken")
    public ResponseEntity<ResponseObject> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
    ) throws Exception {
        User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
        Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Refresh token successfully")
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .username(userDetail.getUsername())
                .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                .id(userDetail.getId()).build();
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .data(loginResponse)
                        .message(loginResponse.getMessage())
                        .status(HttpStatus.OK)
                        .build());
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("imgs/driverUploads/"+imageName);
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


    @PostMapping(value = "license/{id}")
    public ResponseEntity<?> updateLicense(
            @PathVariable("id") Long userId,
            @RequestBody List<Long> licenseIds
    ){
        try{
            User existingUser= userService.getUserById(userId);

            List<UserLicense> userLicenses = new ArrayList<>();

            for(Long license : licenseIds){
                UserLicense userLicense = userService.createUserLicense(
                        existingUser.getId(),
                        UserLicenseDTO.builder()
                                .licenseId(license)
                                .build()
                );

                userLicenses.add(userLicense);
            }
            return ResponseEntity.ok(userLicenses);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable("id") Long userId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            User existingUser= userService.getUserById(userId);
            files = files == null ? new ArrayList<MultipartFile>() : files;

            if(files.size() > UserImage.MAXIMUM_IMAGES_PER_USER){
                return ResponseEntity.badRequest().body("You can only upload maximum 1 images");
            }

            List<UserImage> userImages = new ArrayList<>();

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
                UserImage userImage =  userService.createUserImage(
                        existingUser.getId(),
                        UserImageDTO.builder()
                                .imageUrl(filename)
                                .build());
                userImages.add(userImage);
            }
            return ResponseEntity.ok(userImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

        Path uploadDir = Paths.get("imgs/driverUploads");

        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }

        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }
    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        // Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @PostMapping("/details")
    public ResponseEntity<ResponseObject> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception {
        String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
        User user = userService.getUserDetailsFromToken(extractedToken);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Get user's detail successfully")
                        .data(UserResponse.fromUser(user))
                        .status(HttpStatus.OK)
                        .build()
        );
    }
    @PutMapping("/details/{userId}")
    public ResponseEntity<ResponseObject> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception{
        String extractedToken = authorizationHeader.substring(7);
        User user = userService.getUserDetailsFromToken(extractedToken);

        if (!Objects.equals(user.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User updatedUser = userService.updateUser(userId, updatedUserDTO);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Update user detail successfully")
                        .data(UserResponse.fromUser(updatedUser))
                        .status(HttpStatus.OK)
                        .build()
        );
    }
    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<ResponseObject> resetPassword(@Valid @PathVariable long userId){
        try {
            String newPassword = UUID.randomUUID().toString().substring(0, 5); // Tạo mật khẩu mới
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Reset password successfully")
                    .data(newPassword)
                    .status(HttpStatus.OK)
                    .build());
        } catch (InvalidPasswordException e) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Invalid password")
                    .data("")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("User not found")
                    .data("")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }
    @PutMapping("/block/{userId}/{active}")
    public ResponseEntity<ResponseObject> blockOrEnable(
            @Valid @PathVariable long userId,
            @Valid @PathVariable int active
    ) throws Exception {
        userService.blockOrEnable(userId, active > 0);
        String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message(message)
                .status(HttpStatus.OK)
                .data(null)
                .build());
    }
}
