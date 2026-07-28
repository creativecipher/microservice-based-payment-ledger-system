package com.coder.account_service.entityLayer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bank_details")
public class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id")
    private Long bankId;

    @Column(name = "bank_name",nullable = false)
    private String bankName;

    @Column(name = "branch_name",nullable = false)
    private String branchName;

    @Column(name = "pincode",nullable = false)
    private String pincode;
}



