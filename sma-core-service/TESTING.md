# Testing Guide for SMA Core Service

## Overview

This project uses **JUnit 5** with **Mockito** for unit testing and **Spring Boot Test** for integration testing.

## Test Structure

```
src/test/java/com/sma/core/
├── config/
│   └── TestConfig.java              # Test configuration
├── service/
│   └── impl/
│       ├── UserServiceImplTest.java # Service unit tests
│       └── AuthServiceImplTest.java # Auth service tests
└── resources/
    ├── application-test.yml         # Test configuration
    └── junit-platform.properties    # JUnit 5 configuration
```

## Running Tests

### Run all tests
```bash
./mvnw test
```

### Run specific test class
```bash
./mvnw test -Dtest=UserServiceImplTest
```

### Run specific test method
```bash
./mvnw test -Dtest=UserServiceImplTest#shouldCreateUserSuccessfully
```

### Run with test profile
```bash
./mvnw test -Dspring.profiles.active=test
```

## JUnit 5 Features Used

### 1. Basic Test Structure
```java
@DisplayName("UserService Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        // Setup test data
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Test implementation
    }
}
```

### 2. Nested Tests for Grouping
```java
@Nested
@DisplayName("createUser")
class CreateUserTests {
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() { }
    
    @Test
    @DisplayName("Should throw exception when email exists")
    void shouldThrowExceptionWhenEmailExists() { }
}
```

### 3. Parameterized Tests
```java
@ParameterizedTest(name = "Email: {0}, Expected: {1}")
@CsvSource({
    "valid@test.com, true",
    "invalid-email, false"
})
void shouldValidateEmailFormat(String email, boolean expected) { }
```

### 4. Mockito Integration
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock private Dependency dependency;
    @InjectMocks private MyService service;
    
    @Test
    void test() {
        when(dependency.method()).thenReturn("mocked");
        verify(dependency).method();
    }
}
```

### 5. AssertJ Fluent Assertions
```java
import static org.assertj.core.api.Assertions.*;

assertThat(result)
    .isNotNull()
    .extracting(User::getEmail)
    .isEqualTo("test@test.com");

assertThatThrownBy(() -> service.method())
    .isInstanceOf(AppException.class)
    .hasMessageContaining("error");
```

## Best Practices

### 1. Test Naming
- Use descriptive names: `shouldCreateUserSuccessfully` not `testCreateUser`
- Use `@DisplayName` for readable test reports

### 2. Given-When-Then Structure
```java
@Test
void shouldUpdateUserStatus() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    
    // When
    userService.updateUserStatus(1, UserStatus.INACTIVE);
    
    // Then
    assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
}
```

### 3. Assertions
- Use `assertAll()` for multiple assertions
- Use `assertThatThrownBy()` for exception testing
- Use `verify()` to check mock interactions

### 4. Parallel Execution
Tests run in parallel by default (configured in `junit-platform.properties`):
```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

## Test Coverage

To generate test coverage report:
```bash
./mvnw test jacoco:report
```

Report location: `target/site/jacoco/index.html`

## IntelliJ IDEA Configuration

### Fix Java 23+ Warning

Nếu bạn dùng Java 23+ và thấy warning về "dynamic agent loading", hãy cấu hình VM options:

**Cách 1: Template (khuyến nghị)**
- File `.run/Template JUnit.run.xml` đã được cấu hình sẵn
- IntelliJ sẽ tự động áp dụng cho tất cả JUnit tests

**Cách 2: Manual**
1. Run → Edit Configurations...
2. Tìm JUnit template hoặc test cụ thể
3. Thêm vào VM options:
```
-XX:+EnableDynamicAgentLoading -Djdk.instrument.traceUsage=false
```

**Cách 3: IntelliJ-wide setting**
- Help → Edit Custom VM Options...
- Thêm:
```
-XX:+EnableDynamicAgentLoading
```

## Debugging Tests

### Enable Debug Logging
```bash
./mvnw test -Dlogging.level.com.sma.core=DEBUG
```

### Run Single Test with Output
```bash
./mvnw test -Dtest=UserServiceImplTest#shouldCreateUserSuccessfully -Dsurefire.useFile=false
```

## Common Issues

### 1. Mock Not Working
Ensure using `@ExtendWith(MockitoExtension.class)` or manual instantiation.

### 2. Database Connection Issues
Tests use H2 in-memory database configured in `application-test.yml`.

### 3. Bean Validation Not Working
Add `@AutoConfigureTestDatabase` for repository tests.

## Writing New Tests

### Template for Service Test
```java
package com.sma.core.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("YourService Tests")
@ExtendWith(MockitoExtension.class)
class YourServiceImplTest {

    @Mock
    private Dependency dependency;

    private YourServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new YourServiceImpl(dependency);
    }

    @Nested
    @DisplayName("methodName")
    class MethodNameTests {
        
        @Test
        @DisplayName("Should do something")
        void shouldDoSomething() {
            // Given
            when(dependency.method()).thenReturn(result);
            
            // When
            var actual = service.method();
            
            // Then
            assertThat(actual).isEqualTo(expected);
        }
    }
}
```

## GitHub Actions CI/CD

### Workflow: `test-sma-core-service.yml`

Tự động chạy khi:
- Push lên `main`, `develop`, hoặc `feature/**`
- Pull request vào `main`, `develop`
- Thay đổi trong `sma-backend/sma-core-service/**`

### Jobs:

1. **unit-tests**: Chạy tất cả unit tests với coverage
2. **build**: Build application (chạy sau khi unit-tests pass)

### Status Badge

Thêm vào `README.md`:
```markdown
![SMA Core Service Tests](https://github.com/{owner}/{repo}/actions/workflows/test-sma-core-service.yml/badge.svg)
```

### Local CI Test

Chạy lệnh tương tự CI:
```bash
./mvnw clean test jacoco:report -B
```

## Code Coverage

### Generate Report
```bash
./mvnw test jacoco:report
```

### View Report
Mở `target/site/jacoco/index.html`

### Coverage Thresholds

Thêm vào `pom.xml` để enforce coverage:
```xml
<execution>
    <id>check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.70</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
