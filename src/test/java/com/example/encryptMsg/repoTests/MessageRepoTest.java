package com.example.encryptMsg.repoTests;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.model.Message;
import com.example.encryptMsg.repository.MessageRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepoTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private MessageRepo messageRepo;

    @Test
    void findAllByAccount() {
        Account account = new Account("Chris P. Bacon", new byte[]{1}, new byte[]{2}, new byte[]{3});
        entityManager.persist(account);

        Message message1 = new Message(account, new byte[]{10, 11}, new byte[]{99});
        Message message2 = new Message(account, new byte[]{12, 13}, new byte[]{98});
        entityManager.persist(message1);
        entityManager.persist(message2);

        entityManager.flush();

        List<Message> results = messageRepo.findAllByAccount(account);
        assertEquals(2, results.size());
        assertTrue(results.contains(message1));
        assertTrue(results.contains(message2));
    }

    @Test
    void cascadeDelete() {
        Account account = new Account("To Be Annihilated", new byte[]{1}, new byte[]{2}, new byte[]{3});

        Message msg = new Message(account, new byte[]{10}, new byte[]{20});
        account.addToAssociatedMessagesList(msg);

        entityManager.persist(account);
        entityManager.flush();

        int savedMessageId = msg.getMessageId();
        assertNotNull(entityManager.find(Message.class, savedMessageId), "Message was supposed to be found in the database.");

        entityManager.remove(account);
        entityManager.flush();
        entityManager.clear();

        Message deletedMessage = entityManager.find(Message.class, savedMessageId);
        assertNull(deletedMessage, "Cascade delete has failed.");
    }
}