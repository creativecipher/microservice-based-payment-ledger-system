package com.coder.customer_service.repository;

import com.coder.customer_service.model.CustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails,Long> {

    Optional<CustomerDetails> findByEmailId(String emailId);

}
