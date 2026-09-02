package com.example.encryptMsg.repository;

import com.example.encryptMsg.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    /*
    SQL prompt idea: get all account-data that corresponds to a specific username.

    SELECT account_id, username, password_hash, password_salt, expansion_salt, ciphermode, associated_messages_list
    FROM message
    WHERE username = ?;
     */
}
