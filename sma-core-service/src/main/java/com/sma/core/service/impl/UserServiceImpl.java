package com.sma.core.service.impl;


import com.sma.core.dto.request.user.CreateUserRequest;
import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.dto.response.user.UserDetailResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Company;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import com.sma.core.enums.CandidateShowAs;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.UserMapper;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    CompanyRepository companyRepository;
    PasswordEncoder passwordEncoder;


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

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Integer userId) {
        User user = userRepository.findDetailById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserDetailResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }

        LocalDateTime registrationDate = (request.getJoinedAt() != null)
                ? request.getJoinedAt()
                : LocalDateTime.now();

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail().split("@")[0])
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .lastLoginAt(registrationDate)
                .build();

        if (request.getRole() == Role.CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .user(user)
                    .showAs(CandidateShowAs.RESUME)
                    .isProfilePublic(Boolean.TRUE)
                    .profileCompleteness(0)
                    .build();
            user.setCandidate(candidate);
        } else if (request.getRole() == Role.RECRUITER) {
            if (request.getCompanyId() == null) throw new AppException(ErrorCode.COMPANY_NOT_EXISTED);

            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

            Recruiter recruiter = Recruiter.builder()
                    .user(user)
                    .company(company)
                    .isRootRecruiter(false)
                    .isVerified(true)
                    .build();
            user.setRecruiter(recruiter);
        }

        return userMapper.toAdminResponse(userRepository.save(user));
    }
}