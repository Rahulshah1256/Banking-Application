package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name="beneficiaries")
public class Beneficiary {

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
    private long status;

    @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.ALL})
    @JoinTable(name="beneficiaries_accounts",
            joinColumns =@JoinColumn(name = "beneficiary_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "account_id",referencedColumnName = "id")
    )
    private Set<Account> accounts;

}
