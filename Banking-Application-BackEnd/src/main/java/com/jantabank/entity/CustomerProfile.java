package com.jantabank.entity;

import com.jantabank.domain.enums.Gender;
import com.jantabank.domain.enums.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Extended customer profile held 1:1 with a {@link User}. Core identity fields
 * (name, email, PAN, Aadhaar, mobile, address) remain on User; this holds the
 * richer NetBanking profile attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "annual_income")
    private Double annualIncome;

    @Column(name = "communication_address", length = 500)
    private String communicationAddress;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "profile_photo_path", length = 255)
    private String profilePhotoPath;

    @Column(name = "profile_photo_content_type", length = 100)
    private String profilePhotoContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", length = 20, nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;
}
