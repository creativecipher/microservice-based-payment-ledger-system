package com.coder.account_service.repository;

import com.coder.account_service.dataLayer.model.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<BankDetails,Long> {
}



