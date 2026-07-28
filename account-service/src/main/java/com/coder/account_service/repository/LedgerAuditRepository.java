package com.coder.account_service.repository;

import com.coder.account_service.entityLayer.model.LedgerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerAuditRepository extends JpaRepository<LedgerAudit,Long> {

}
