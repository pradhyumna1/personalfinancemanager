package com.example.pfm_dashboard.controller;

import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.service.AccountService;
import com.example.pfm_dashboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.pfm_dashboard.dto.AccountBankDTO;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Account>> getAccountsByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/user/{username}/accounts-with-banks")
    public ResponseEntity<List<AccountBankDTO>> getAccountNamesWithBanks(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<AccountBankDTO> accountNamesWithBanks = accountService.getAccountNamesWithBanks(user.getId());
        return ResponseEntity.ok(accountNamesWithBanks);
    }
    



    @PostMapping("/save")
    public ResponseEntity<Account> saveAccount(@RequestBody Account account, @RequestParam String username) {
        User user = userService.findUserByUsername(username);
        account.setUser(user);
        Account savedAccount = accountService.saveAccount(account);
        return ResponseEntity.ok(savedAccount);
    }
}
