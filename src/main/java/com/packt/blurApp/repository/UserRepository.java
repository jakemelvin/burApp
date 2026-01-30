package com.packt.blurApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    // Role references (both new many-to-many and legacy many-to-one)
    java.util.List<User> findAllByRole(com.packt.blurApp.model.Role role);

    java.util.List<User> findAllByRolesContains(com.packt.blurApp.model.Role role);

}
