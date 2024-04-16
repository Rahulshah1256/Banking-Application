package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name="transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "fromaccount",nullable = false)
    private String fromaccount;

    @Column(name = "toaccount",nullable = false)
    private String toaccount;


    @Column(name = "amount",nullable = false)
    private double amount;

    @Column(name = "transactiondate",nullable = false)
    private Date transactiondate;

    @Column(name = "status",nullable = false)
    private long status;
}
