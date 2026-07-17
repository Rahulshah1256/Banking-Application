package com.jantabank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import com.jantabank.domain.enums.TransactionStatus;
import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name="transactions")
public class Transaction extends BaseEntity {

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
    private TransactionStatus status;

    @Column(name = "reference_number", length = 40, unique = true)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_mode", length = 20)
    private TransferMode transferMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "channel", length = 20)
    private String channel;

    @Column(name = "initiated_by_user_id")
    private Long initiatedByUserId;
}
