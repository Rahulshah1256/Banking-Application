package com.jantabank.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name="accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "accountnumber",nullable = false)
    private String accountNumber;

    @Column(name = "accountholdername",nullable = false)
    private String accountHolderName;

    @Column(name = "accounttype",nullable = false)
    private String accountType;

    @Column(name = "branchid",nullable = false)
    private String branchId;

    @Column(name = "ifsccode",nullable = false)
    private String ifscCode;

    @Column(name = "balance",nullable = false)
    private double balance;

    @Column(name = "opendate",nullable = false)
    private Date openDate;

    @Column(name = "status",nullable = false)
    private long status;

    @Column(name = "address",nullable = false)
    private String address;

    @Column(name = "contactnumber",nullable = false)
    private String contactNumber;

    @Column(name = "emailaddress",nullable = false)
    private String emailAddress;

    @Column(name = "nominee",nullable = false)
    private String nominee;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name="accounts_users",
            joinColumns =@JoinColumn(name = "account_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id")
    )
    private Set<User> users;

}

