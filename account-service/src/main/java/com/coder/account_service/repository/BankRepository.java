package com.coder.account_service.repository;

import com.coder.account_service.entityLayer.model.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<BankDetails,Long> {
}



