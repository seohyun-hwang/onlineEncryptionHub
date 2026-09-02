package com.example.encryptMsg.crackingTests;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.repository.AccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class Injection_SQL_Tests {
    @Autowired
    private AccountRepo accountRepo;

    @Test
    void findByUsername_sqlInjectionAttempt() {
        Account innocent = new Account("Tom", new byte[]{1}, new byte[]{2}, new byte[]{3}, "GCM");
        accountRepo.save(innocent);

        Optional<Account> cracker1 = accountRepo.findByUsername("admin' OR '1'='1");

        assertTrue(cracker1.isEmpty(), "Query was manipulated by SQL injection");

        // Little Bobby Tables payload
        Account cracker2 = new Account("Jerry'; DROP TABLE account;--", new byte[]{1}, new byte[]{2}, new byte[]{3}, "CBC");
        accountRepo.save(cracker2);
        // database should've stored the 'injection' as a literal.
        Optional<Account> safelyStoredAccount = accountRepo.findByUsername("Jerry'; DROP TABLE account;--");

        assertTrue(safelyStoredAccount.isPresent());
        assertEquals("Jerry'; DROP TABLE account;--", safelyStoredAccount.get().getUsername(),
                "Failed to store SQL-payload as String-literal");

        assertTrue(accountRepo.findByUsername("Tom").isPresent());
    }
}
