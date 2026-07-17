package com.jantabank.dto.cheque;

import com.jantabank.domain.enums.ChequeBookStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChequeBookResponse {
    private Long id;
    private String bookReferenceNumber;
    private String accountNumber;
    private int numberOfLeaves;
    private long startChequeNumber;
    private long endChequeNumber;
    private ChequeBookStatus status;
    private String deliveryAddress;
    private LocalDateTime requestedAt;
    private LocalDateTime issuedAt;
    private LocalDateTime deliveredAt;
    private List<ChequeResponse> leaves;
}
