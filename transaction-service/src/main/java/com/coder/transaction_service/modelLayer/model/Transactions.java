package com.coder.transaction_service.modelLayer.model;

import com.coder.transaction_service.custom.exceptions.BadRequestException;
import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import com.coder.transaction_service.modelLayer.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transactions {
    @Id
    @Column(name = "trans_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long trasId;

    @Column(name = "trans_code",unique = true,nullable = false)
    private String transCode;

    @Column(name = "from_acc",nullable = false)
    private Long fromAccount;

    @Column(name = "to_acc")
    private Long toAccount;

    @Column(name = "trans_amount",nullable = false)
    private BigDecimal amount;

    @Column(name = "trans_status",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus transStatus;

    @Column(name = "trans_type",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionType transType;

    @Column(name = "trans_created_at",nullable = false,updatable = false)
    private LocalDateTime transCreatedAt;

    public void setAmount(BigDecimal amount){
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0){
            throw new BadRequestException("Invalid Amount!");
        }
        this.amount=amount;
    }
}
