package com.jantabank.dto.cheque;

import com.jantabank.domain.enums.ChequeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChequeResponse {
    private Long id;
    private Long chequeBookId;
    private String accountNumber;
    private long chequeNumber;
    private ChequeStatus status;
    private String stopReason;
    private LocalDateTime stoppedAt;
    private boolean positivePayRegistered;
    private Double positivePayAmount;
    private String positivePayPayee;
    private LocalDate positivePayDate;
}
