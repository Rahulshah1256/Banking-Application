package com.jantabank.repository;

import com.jantabank.entity.User;
import com.jantabank.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username,String Email);
    Optional<User> findByEmail(String email);

    Optional<User> findById(long Id);
    Boolean existsByUsername(String userName);

    List<User> findByStatus(UserStatus status);

    long countByStatus(UserStatus status);

}

