package com.jantabank.service;

import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.upi.RegisterUpiHandleRequest;
import com.jantabank.dto.upi.UpiHandleDto;
import com.jantabank.dto.upi.UpiPayRequest;
import com.jantabank.dto.upi.UpiResolveDto;

import java.util.List;

/**
 * UPI operations: VPA registration/lookup, VPA-addressed payments and QR
 * (collect) code generation.
 */
public interface UpiService {

    UpiHandleDto registerHandle(RegisterUpiHandleRequest request, String username);

    List<UpiHandleDto> listHandles(String username);

    UpiResolveDto resolve(String vpa);

    TransactionReceiptDto pay(UpiPayRequest request, String username);

    byte[] generateCollectQr(String vpa, Double amount, String note, String username);
}
