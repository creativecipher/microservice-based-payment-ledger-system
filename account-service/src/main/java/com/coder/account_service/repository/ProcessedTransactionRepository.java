package com.coder.account_service.repository;

import com.coder.account_service.dataLayer.model.ProcessedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedTransactionRepository extends JpaRepository<ProcessedTransaction,Long> {

    Optional<ProcessedTransaction> findByTransCode(String transCode);

    boolean existsByTransCode(String transCode);
}
