package com.example.pfm_dashboard.service;
import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.example.pfm_dashboard.dto.AccountBankDTO;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ItemService itemService;

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUser_Id(userId);
    }

    public List<Account> getAccountsByItemId(String itemId) {
        return accountRepository.findByItem_ItemId(itemId);
    }

    public List<Account> getAccountsByAccessToken(String accessToken) {
        // Use the ItemService to get the Item associated with the accessToken
        Item item = itemService.getItemByAccessToken(accessToken);
    
        // Retrieve the accounts using the Item's itemId
        return getAccountsByItemId(item.getItemId());
    }
    

    public List<AccountBankDTO> getAccountNamesWithBanks(Long userId) {
        return accountRepository.findByUser_Id(userId).stream()
                .map(account -> {
                    String accountName = account.getName() != null ? account.getName() : "Unknown Account";
                    String bankName = account.getItem() != null && account.getItem().getBankName() != null
                            ? account.getItem().getBankName()
                            : "Unknown Bank";
                    return new AccountBankDTO(accountName, bankName);
                })
                .collect(Collectors.toList());
    }
    

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
}
