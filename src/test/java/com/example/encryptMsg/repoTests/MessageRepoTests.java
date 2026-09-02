package com.example.encryptMsg.repoTests;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.model.Message;
import com.example.encryptMsg.repository.AccountRepo;
import com.example.encryptMsg.repository.MessageRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepoTests {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Test
    void findAllByAccount() {
        Account account = new Account("Chris P. Bacon", new byte[]{1}, new byte[]{2}, new byte[]{3}, "CBC");
        accountRepo.save(account);

        Message message1 = new Message(account, new byte[]{10, 11}, new byte[]{99});
        Message message2 = new Message(account, new byte[]{12, 13}, new byte[]{98});
        messageRepo.save(message1);
        messageRepo.save(message2);

        List<Message> results = messageRepo.findAllByAccount(account);
        assertEquals(2, results.size());
        assertTrue(results.contains(message1));
        assertTrue(results.contains(message2));
    }

    @Test
    void cascadeDelete() {
        Account account = new Account("To Be Annihilated", new byte[]{1}, new byte[]{2}, new byte[]{3}, "GCM");
        Message msg = new Message(account, new byte[]{10}, new byte[]{20});
        account.addToAssociatedMessagesList(msg);

        accountRepo.save(account);

        int savedMessageId = msg.getMessageId();
        assertTrue(messageRepo.findById(savedMessageId).isPresent());
        accountRepo.delete(account); // supposed to trigger cascade deletion

        assertFalse(messageRepo.findById(savedMessageId).isPresent(), "Cascade delete failed!");
    }
}