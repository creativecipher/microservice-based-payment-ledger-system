package com.coder.account_service.repository;

import com.coder.account_service.entityLayer.model.ProcessedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProcessedTransactionRepository extends JpaRepository<ProcessedTransaction,Long> {

    @Query("  SELECT t.transCode FROM ProcessedTransaction t " +
            " WHERE t.transCode in ( :transCodes ) ")
    Optional<Set<String>> findByTransCodes(@Param("transCodes") Set<String> transCodes);

//    boolean existsByTransCode(String transCode);
}
