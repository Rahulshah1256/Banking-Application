package com.jantabank.domain.enums;

/**
 * Rail / channel used to move money. WITHIN_BANK is an internal book transfer;
 * IMPS/NEFT/RTGS/UPI mirror the Indian interbank payment rails.
 */
public enum TransferMode {
    WITHIN_BANK,
    IMPS,
    NEFT,
    RTGS,
    UPI
}
