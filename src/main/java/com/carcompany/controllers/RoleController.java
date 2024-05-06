package com.carcompany.controllers;

import com.carcompany.models.Role;
import com.carcompany.responses.ResponseObject;
import com.carcompany.services.role.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService roleService;

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("") // http://localhost:8080/api/v1/roles
    public ResponseEntity<ResponseObject> getAllRoles(){
        List<Role> roles=  roleService.getAllRoles();
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get all roles successfully")
                        .status(HttpStatus.OK)
                        .data(roles)
                        .build());
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
}
