package com.sma.core.service;

import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.dto.response.user.UserDetailResponse;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserAdminResponse> getAllUsersForAdmin(String email, Role role, UserStatus status, Pageable pageable);
    void updateUserStatus(Integer userId, UserStatus status);
    UserDetailResponse getUserDetail(Integer userId);
}
