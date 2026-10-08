package com.secureproxy.repository;

import com.secureproxy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    boolean existsByName(String name);

    Optional<User> findByNameAndRole(String name, String role);

    List<User> findByRole(String role);

    List<User> findByRoleAndStatus(String role, String status);
}