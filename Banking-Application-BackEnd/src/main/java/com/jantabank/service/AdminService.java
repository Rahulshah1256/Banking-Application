package com.jantabank.service;

import com.jantabank.domain.enums.TransferMode;
import com.jantabank.domain.enums.UserStatus;
import com.jantabank.dto.admin.AdminOverviewResponse;
import com.jantabank.dto.admin.AdminTransactionView;
import com.jantabank.dto.admin.AdminUserDetail;
import com.jantabank.dto.admin.AdminUserSummary;
import com.jantabank.dto.report.TransactionReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminService {

    AdminOverviewResponse overview();

    Page<AdminUserSummary> listUsers(UserStatus status, String query, Pageable pageable);

    AdminUserDetail getUserDetail(Long userId);

    AdminUserSummary updateUserStatus(Long userId, UserStatus status);

    Page<AdminTransactionView> transactions(LocalDate from, LocalDate to, TransferMode mode, Pageable pageable);

    TransactionReportResponse transactionReport(LocalDate from, LocalDate to);
}
