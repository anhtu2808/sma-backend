package com.sma.core.repository;

import com.sma.core.entity.User;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:email IS NULL OR u.email ILIKE CONCAT('%', CAST(:email AS string), '%')) AND " +
            "(CAST(:role AS string) IS NULL OR u.role = :role) AND " +
            "(CAST(:status AS string) IS NULL OR u.status = :status)")
    Page<User> findAllAdmin(
            @Param("email") String email,
            @Param("role") Role role,
            @Param("status") UserStatus status,
            Pageable pageable
    );

}
