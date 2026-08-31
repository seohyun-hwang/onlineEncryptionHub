package com.example.encryptMsg.repository;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Integer> {
    List<Message> findAllByAccount(Account account);
    /*
    SQL prompt idea: get all message-data from a *list* of messages that corresponds to a specific account ID.

    SELECT message_id, account_id, message_ciphertext, initialization_vector
    FROM account
    WHERE account_id = ?;
     */
}
