package com.coder.account_service.dataLayer.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
//Explicitly define the unique constraint index for performance and easier log tracing
@Table(
        name = "processed_transaction",
        indexes = {
                @Index(name = "uk_processed_trans_code",columnList = "trans_code",unique = true)
        }
)
public class ProcessedTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processed_trans_id")
    @Setter(AccessLevel.NONE) // prevent application code from manually modifying the auto-generated PK
    private Long processedTransId;

    @Column(name = "from_account_id",nullable = false)
    private Long fromAccount;

    @Column(name = "to_account_id",nullable = false)
    private Long toAccount;

    @Column(name = "trans_code",unique = true,nullable = false)
    private String transCode;

    @Column(name = "trans_amount",nullable = false)
    private BigDecimal amount;

    @Column(name = "processed_trans_created_at",nullable = false,updatable = false)
    private LocalDateTime processedTransCreatedAt;
}
