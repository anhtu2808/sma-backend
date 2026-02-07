package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.dto.response.user.UserDetailResponse;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping
    //    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {

        return ApiResponse.<Page<UserAdminResponse>>builder()
                .message("Users fetched successfully")
                .data(userService.getAllUsersForAdmin(email, role, status, pageable))
                .build();
    }


    @PatchMapping("/{userId}/status")
    //    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatus(
            @PathVariable Integer userId,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(userId, status);
        return ApiResponse.<Void>builder()
                .message("User status updated to " + status)
                .build();
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDetailResponse> getUserDetail(@PathVariable Integer id) {
        return ApiResponse.<UserDetailResponse>builder()
                .message("Get user dossier successfully")
                .data(userService.getUserDetail(id))
                .build();
    }
}
