package com.example.encryptMsg.repoTests;


import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.repository.AccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepoTest {

    @Autowired
    private AccountRepo accountRepo;

    @Test
    void findByUsername_accountFound() {
        byte[] trialHash = new byte[]{1, 2, 3};
        byte[] trialPasswordSalt = new byte[]{4, 5, 6};
        byte[] trialExpansionSalt = new byte[]{7, 8, 9};

        Account account = new Account("John Pork", trialHash, trialPasswordSalt, trialExpansionSalt);
        accountRepo.save(account);

        Optional<Account> accountOptional = accountRepo.findByUsername("John Pork");
        assertTrue(accountOptional.isPresent());
        assertEquals("John Pork", accountOptional.get().getUsername());
        assertArrayEquals(trialHash, accountOptional.get().getPasswordHash());
    }

    @Test
    void findByUsername_accountMissing() {
        Optional<Account> accountOptional = accountRepo.findByUsername("nDSDjkNfdFdlNF");
        assertTrue(accountOptional.isEmpty());
    }
}