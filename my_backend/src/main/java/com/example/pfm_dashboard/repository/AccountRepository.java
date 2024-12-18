package com.example.pfm_dashboard.repository;

import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser_Id(Long userId); // Find all accounts for a user
    List<Account> findByItem_ItemId(String itemId);
    Optional<Account> findByAccountId(String accountId); // Find account by its unique Plaid ID
    boolean existsByAccountId(String accountId);

    Optional<Account> findByNameAndUserId(String accountId, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);


}
