package com.sma.core.service.impl;

import com.sma.core.dto.request.user.CreateUserRequest;
import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.dto.response.user.UserDetailResponse;
import com.sma.core.entity.User;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.UserMapper;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    private User testUser;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        // Manual constructor injection for proper mocking
        userService = new UserServiceImpl(
                userRepository, 
                userMapper, 
                companyRepository, 
                passwordEncoder
        );

        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .fullName("Test User")
                .passwordHash("encoded_password")
                .role(Role.CANDIDATE)
                .status(UserStatus.ACTIVE)
                .build();

        createUserRequest = CreateUserRequest.builder()
                .email("new@example.com")
                .fullName("New User")
                .password("password123")
                .role(Role.CANDIDATE)
                .build();
    }

    @Nested
    @DisplayName("getAllUsersForAdmin")
    class GetAllUsersForAdminTests {

        @Test
        @DisplayName("Should return paginated users for admin")
        void shouldReturnPaginatedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<User> users = List.of(testUser);
            Page<User> userPage = new PageImpl<>(users, pageable, users.size());
            UserAdminResponse response = UserAdminResponse.builder()
                    .email(testUser.getEmail())
                    .fullName(testUser.getFullName())
                    .build();

            when(userRepository.findAllAdmin(isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(userPage);
            when(userMapper.toAdminResponse(any(User.class))).thenReturn(response);

            // When
            Page<UserAdminResponse> result = userService.getAllUsersForAdmin(
                    null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail())
                    .isEqualTo(testUser.getEmail());
            verify(userRepository).findAllAdmin(isNull(), isNull(), isNull(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("Should update user status successfully")
        void shouldUpdateUserStatus() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.updateUserStatus(1, UserStatus.INACTIVE);

            // Then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
            verify(userRepository).findById(1);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(AppException.class, () ->
                    userService.updateUserStatus(999, UserStatus.INACTIVE)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getUserDetail")
    class GetUserDetailTests {

        @Test
        @DisplayName("Should return user detail when user exists")
        void shouldReturnUserDetail() {
            // Given
            UserAdminResponse baseInfo = UserAdminResponse.builder()
                    .email(testUser.getEmail())
                    .fullName(testUser.getFullName())
                    .build();
            UserDetailResponse expectedResponse = UserDetailResponse.builder()
                    .baseInfo(baseInfo)
                    .build();

            when(userRepository.findDetailById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserDetailResponse(testUser)).thenReturn(expectedResponse);

            // When
            UserDetailResponse result = userService.getUserDetail(1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBaseInfo().getEmail())
                    .isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when user detail not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findDetailById(999)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserDetail(999))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email not exists")
        void shouldCreateUserSuccessfully() {
            // Given
            String encodedPassword = "encoded_password_123";
            UserAdminResponse expectedResponse = UserAdminResponse.builder()
                    .email(createUserRequest.getEmail())
                    .fullName(createUserRequest.getFullName())
                    .build();

            when(userRepository.existsByEmail(createUserRequest.getEmail()))
                    .thenReturn(false);
            when(passwordEncoder.encode(createUserRequest.getPassword()))
                    .thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(2);
                return savedUser;
            });
            when(userMapper.toAdminResponse(any(User.class))).thenReturn(expectedResponse);

            // When
            UserAdminResponse result = userService.createUser(createUserRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(createUserRequest.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(createUserRequest.getEmail()))
                    .thenReturn(true);

            // When & Then
            AppException exception = assertThrows(AppException.class, () ->
                    userService.createUser(createUserRequest)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTS);
        }
    }
}
