package com.sma.core.service.impl;


import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.entity.User;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.UserMapper;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getAllUsersForAdmin(String email, Role role, UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.findAllAdmin(email, role, status, pageable);
        return users.map(userMapper::toAdminResponse);
    }

    @Override
    public void updateUserStatus(Integer userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(status);
        userRepository.save(user);
    }
}