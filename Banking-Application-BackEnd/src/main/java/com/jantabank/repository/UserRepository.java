package com.jantabank.repository;

import com.jantabank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username,String Email);

    Optional<User> findById(long Id);
    Boolean existsByUsername(String userName);

    @Query("SELECT u FROM User u WHERE u.status = :statusValue")
    List<User> findUsersByStatus(@Param("statusValue") int status);

}

