package com.coder.account_service.repository;

import com.coder.account_service.entityLayer.model.CustomerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<CustomerAccount,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CustomerAccount c " +
            "WHERE c.accountId = :id")
    Optional<CustomerAccount> findByIdForUpdate(@Param("id") Long id);


}



