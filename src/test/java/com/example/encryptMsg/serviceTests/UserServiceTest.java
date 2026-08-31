package com.example.encryptMsg.serviceTests;

import com.example.encryptMsg.model.*;
import com.example.encryptMsg.repository.*;
import com.example.encryptMsg.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AccountRepo accountRepo;
    @Mock
    private MessageRepo messageRepo;
    @Mock
    private EncryptionService encryptionService;
    @InjectMocks
    private UserService userService;

    @Test
    void createMessage_userNotFound() { // createMessage should return 'false'.
        when(accountRepo.findByUsername("Spooky Ghost")).thenReturn(Optional.empty());
        boolean result = userService.createMessage("Spooky Ghost", "Bah!", "ssshhhhh");
        assertFalse(result);
        verify(messageRepo, never()).save(any(Message.class));
    }

    @Test
    void createMessage_wrongPassword() { // createMessage should return 'false'.
        Account trialAccount = new Account("Patrick Star", new byte[]{1}, new byte[]{2}, new byte[]{3});

        when(accountRepo.findByUsername("Patrick Star")).thenReturn(Optional.of(trialAccount));
        when(encryptionService.keySaltedStretch(any(), anyInt(), any())).thenReturn(new byte[]{99}); // some weird hash that will differ from the hash of the real password
        boolean result = userService.createMessage("Patrick Star", "Buuuuhhhhhhh", "Heeyyy");

        assertFalse(result);
        verify(messageRepo, never()).save(any(Message.class));
    }

    @Test
    void createMessage_validData() { // createMessage should save all data and return 'true'.
        byte[] hash = new byte[]{1};
        Account trialAccount = new Account("Izzeme Mario", hash, new byte[]{2}, new byte[]{3});

        when(accountRepo.findByUsername("Izzeme Mario")).thenReturn(Optional.of(trialAccount));

        when(encryptionService.keySaltedStretch(any(), anyInt(), any())).thenReturn(hash);
        when(encryptionService.rijndael256expansion(any())).thenReturn(new int[]{});
        when(encryptionService.aes256encryptionGCM(any(), any(), any())).thenReturn(new byte[]{42});

        boolean result = userService.createMessage("Izzeme Mario", "Yiieeepiyeahh!", "password0123456789");
        assertTrue(result);
        verify(messageRepo, times(1)).save(any(Message.class));
    }
}