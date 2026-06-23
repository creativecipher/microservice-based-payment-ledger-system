package com.coder.account_service.dataLayer.model;

import com.coder.account_service.custom.exceptions.BadRequestException;
import com.coder.account_service.dataLayer.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "customer_account")
public class CustomerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "customer_id",nullable = false)
    private Long customerId;

//    @Column(name = "bank_id",nullable = false)
//    private Long bankId;
    @ManyToOne
    @JoinColumn(name = "bank_id",nullable = false)
    private BankDetails bankDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type",nullable = false)
    private AccountType accountType;

    @Column(name = "balance",nullable = false)
    private BigDecimal balance;

    public void setBalance(BigDecimal balance){
        // check if balance is less than 0
        if(balance.compareTo(BigDecimal.ZERO)<0) throw new BadRequestException("Invalid Balance value!");

        this.balance = balance;
    }

}




