package com.coder.account_service.entityLayer.model;

import com.coder.account_service.entityLayer.enums.AccountType;
import com.coder.account_service.entityLayer.enums.EntryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "ledger_audit")
public class LedgerAudit {
    @Id
    @Column(name = "audit_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLog;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "trans_code",nullable = false) //unique = true-> transCode is not unique (as it can be present in 2 record -> Credit and Debit)
    private String transCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type",nullable = false)
    private EntryType entryType;

    @Column(name = "trans_amount",nullable = false)
    @DecimalMin(value="0.01",inclusive = true,message = "Amount must be greater than 0")
    @DecimalMax(value="50000.00",message = "Amount cannot exceed 50000")
    private BigDecimal amount;

    @Column(name = "account_balance_before",nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "account_balance_after",nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "ledger_audit_created_at",nullable = false,updatable = false)
    private LocalDateTime ledgerAuditCreatedAt=LocalDateTime.now();
}
