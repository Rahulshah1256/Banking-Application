package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import com.jantabank.domain.enums.BeneficiaryStatus;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name="beneficiaries")
public class Beneficiary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "beneficiaryaccountnumber",nullable = false)
    private String beneficiaryaccountnumber;

    @Column(name = "beneficiaryaccountname",nullable = false)
    private String beneficiaryaccountname;

    @Column(name = "beneficiaryaccountifsc",nullable = false)
    private String beneficiaryaccountifsc;

    @Column(name = "amountlimit",nullable = false)
    private double amountlimit;

    @Column(name = "status",nullable = false)
    private BeneficiaryStatus status;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "favourite", nullable = false)
    private boolean favourite;

    @Column(name = "activate_after")
    private java.time.LocalDateTime activateAfter;

    @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name="beneficiaries_accounts",
            joinColumns =@JoinColumn(name = "beneficiary_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "account_id",referencedColumnName = "id")
    )
    private Set<Account> accounts;

}
