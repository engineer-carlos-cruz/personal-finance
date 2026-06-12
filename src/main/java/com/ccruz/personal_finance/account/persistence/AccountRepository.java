package com.ccruz.personal_finance.account.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByCode(String code);
    boolean existsByCode(String code);

    @Query(value = "SELECT * FROM accounts", nativeQuery = true)
    List<Account> findAllIncludingInactive();
}
