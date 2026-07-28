package com.coder.transaction_service.dataLayer;

import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import com.coder.transaction_service.modelLayer.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions,Long> {

    Optional<Transactions> findByTransCode(String transCode);

    @Query("SELECT t.transCode FROM Transactions t " +
            " WHERE t.transStatus in :transStatus " +
            " AND t.transCreatedAt <= :recentTime")
    Optional<Set<String>> findByTransStatusAndTransCreatedAt(@Param("transStatus") List<TransactionStatus> transStatus, @Param("recentTime") LocalDateTime recentTime);

    @Modifying
    @Query("UPDATE Transactions t " +
            " SET t.transStatus = :status " +
            " WHERE t.transCode in :transCode ")
    int updateTransactionStatus(@Param("transCode") Set<String> transCode,@Param("status") TransactionStatus status);

}
