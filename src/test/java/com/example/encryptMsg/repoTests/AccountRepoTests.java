package com.example.encryptMsg.repoTests;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.repository.AccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepoTests {

    @Autowired
    private AccountRepo accountRepo;

    @Test
    void findByUsername_accountFoundGCM() {
        byte[] trialHash = new byte[]{1, 2, 3};
        byte[] trialPasswordSalt = new byte[]{4, 5, 6};
        byte[] trialExpansionSalt = new byte[]{7, 8, 9};

        Account account = new Account("John Pork", trialHash, trialPasswordSalt, trialExpansionSalt, "GCM");
        accountRepo.save(account);

        Optional<Account> accountOptional = accountRepo.findByUsername("John Pork");
        assertTrue(accountOptional.isPresent());
        assertEquals("John Pork", accountOptional.get().getUsername());
        assertArrayEquals(trialHash, accountOptional.get().getPasswordHash());
        assertEquals("GCM", accountOptional.get().getCiphermode());
    }
    @Test
    void findByUsername_accountFoundCBC() {
        byte[] trialHash = new byte[]{1, 2, 3};
        byte[] trialPasswordSalt = new byte[]{4, 5, 6};
        byte[] trialExpansionSalt = new byte[]{7, 8, 9};

        Account account = new Account("John Pork", trialHash, trialPasswordSalt, trialExpansionSalt, "CBC");
        accountRepo.save(account);

        Optional<Account> accountOptional = accountRepo.findByUsername("John Pork");
        assertTrue(accountOptional.isPresent());
        assertEquals("John Pork", accountOptional.get().getUsername());
        assertArrayEquals(trialHash, accountOptional.get().getPasswordHash());
        assertEquals("CBC", accountOptional.get().getCiphermode());
    }

    @Test
    void findByUsername_accountMissing() {
        Optional<Account> accountOptional = accountRepo.findByUsername("Spooky Shhmookie Ghost Y'all");
        assertTrue(accountOptional.isEmpty());
    }
}