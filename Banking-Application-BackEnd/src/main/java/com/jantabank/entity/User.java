package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.jantabank.domain.enums.UserStatus;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "aadhaarno",nullable = false)
    private String aadhaarNo;

    @Column(name = "panno",nullable = false)
    private String panNo;

    @Column(name = "address",nullable = false)
    private String address;

    @Column(name = "mobile",nullable = false)
    private String mobile;

    @Column(name = "status",nullable = false)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "lock_until")
    private java.time.LocalDateTime lockUntil;

    public boolean isLocked() {
        return lockUntil != null && lockUntil.isAfter(java.time.LocalDateTime.now());
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="users_roles",
            joinColumns =@JoinColumn(name = "user_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id")
    )
    private Set<Role> roles;
}

