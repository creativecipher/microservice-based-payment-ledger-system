package com.coder.transaction_service.dataLayer;

import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import com.coder.transaction_service.modelLayer.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions,Long> {

    Optional<Transactions> findByTransCode(String transCode);

//    @Modifying
//    @Query("UPDATE Transactions t " +
//            " SET t.transStatus = :status " +
//            " WHERE t.transCode = :transCode")
//    int updateTransactionStatus(@Param("transCode") String transCode,@Param("status") TransactionStatus status);



}
