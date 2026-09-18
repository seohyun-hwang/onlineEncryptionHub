package com.example.encryptMsg.service;

import com.example.encryptMsg.cryptography.CryptographyToggleInterface;
import com.example.encryptMsg.cryptography.IV_and_Ciphertext;
import com.example.encryptMsg.model.*;
import com.example.encryptMsg.payload.CreateMessageResponse;
import com.example.encryptMsg.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.*;

@Service
public class UserService {
    private final AccountRepo accountRepo;
    private final MessageRepo messageRepo;
    private final CryptographyToggleInterface cryptographyToggleInterface;
    private static final SecureRandom secureRandom = new SecureRandom();

    public UserService(
            AccountRepo accountRepo,
            MessageRepo messageRepo,

            /*
            // the @Qualifier argument is "custom" by default.
            // qualifier argument is "custom" --> my custom cryptography code is activated.
            // qualifier argument is "library" --> library-based cryptography code is activated.
             */
            @Qualifier("custom") CryptographyToggleInterface cryptographyToggleInterface)
    {
        this.accountRepo = accountRepo;
        this.messageRepo = messageRepo;
        this.cryptographyToggleInterface = cryptographyToggleInterface;
    }

    public int createAccount(String username, char[] password, String ciphermode) throws Exception {
        byte[] passwordSalt = new byte[32];
        byte[] expansionSalt = new byte[32];
        secureRandom.nextBytes(passwordSalt);
        secureRandom.nextBytes(expansionSalt);

        try {
            Account newAccount = new Account(
                    username,
                    cryptographyToggleInterface.passwordHashingSHA256(password, passwordSalt),
                    passwordSalt,
                    expansionSalt,
                    ciphermode
            );
            accountRepo.save(newAccount);
            return newAccount.getAccountId();
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @Transactional
    public CreateMessageResponse createMessage(String username, char[] messagePlaintext, char[] password) throws Exception {
        try {
            Account account = accountRepo.findByUsername(username).orElse(null);
            if (account == null || !cryptographyToggleInterface.passwordCheck(password, account.getPasswordSalt(), account.getPasswordHash())) {
                return null;
            }
            IV_and_Ciphertext result = cryptographyToggleInterface.encryptionAES(
                    messagePlaintext,
                    password,
                    account.getExpansionSalt(),
                    account.getCiphermode()
            );

            Message newMessage = new Message(account, result.ciphertext(), result.iv());
            messageRepo.save(newMessage);

            return new CreateMessageResponse(account.getCiphermode(), newMessage.getMessageId());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(messagePlaintext, '\0');
        }
    }

    @Transactional
    public int deleteAccount(String username, char[] password) throws Exception {
        try {
            Account account = accountRepo.findByUsername(username).orElse(null);

            if (account == null || !cryptographyToggleInterface.passwordCheck(password, account.getPasswordSalt(), account.getPasswordHash())) {
                return 0;
            }

            int accountId = account.getAccountId();
            accountRepo.delete(account);
            return accountId;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public Map<Integer, char[]> getAllStoredPlaintext_byAccount(String username, char[] password) throws Exception {
        Map<Integer, char[]> mapToReturn = new HashMap<>();

        try {
            Account account = accountRepo.findByUsername(username).orElse(null);

            if (account == null || !cryptographyToggleInterface.passwordCheck(password, account.getPasswordSalt(), account.getPasswordHash())) {
                return null;
            }

            for (Message message : messageRepo.findAllByAccount(account)) {
                char[] plaintextChars = cryptographyToggleInterface.decryptionAES(
                        message.getMessageCiphertext(),
                        message.getInitializationVector(),
                        password,
                        account.getExpansionSalt(),
                        account.getCiphermode()
                );
                mapToReturn.put(message.getMessageId(), plaintextChars);
            }

            return mapToReturn;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @Transactional
    public boolean deleteMessage(String username, int messageId, char[] password) throws Exception {
        try {
            Account account = accountRepo.findByUsername(username).orElse(null);

            if (account == null || !cryptographyToggleInterface.passwordCheck(password, account.getPasswordSalt(), account.getPasswordHash())) {
                return false;
            }
            Message message = getMessageById(messageId);

            if (message.getAccount().getAccountId() != account.getAccountId()) {
                return false;
            }

            messageRepo.delete(message);
            return true;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public Message getMessageById(int messageId) {
        return messageRepo.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found with ID " + messageId + "."));
    }
    /*
    public Optional<Account> findByUsername(String username) {
        return accountRepo.findByUsername(username);
    }
     */
}