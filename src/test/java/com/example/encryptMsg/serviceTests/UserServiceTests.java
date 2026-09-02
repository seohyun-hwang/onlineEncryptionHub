package com.example.encryptMsg.serviceTests;

import com.example.encryptMsg.cryptography.CryptographyToggle;
import com.example.encryptMsg.cryptography.IV_and_Ciphertext;
import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.model.Message;
import com.example.encryptMsg.payload.CreateMessageResponse;
import com.example.encryptMsg.repository.AccountRepo;
import com.example.encryptMsg.repository.MessageRepo;
import com.example.encryptMsg.service.UserService;
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
class UserServiceTests {

    @Mock
    private AccountRepo accountRepo;
    @Mock
    private MessageRepo messageRepo;
    @Mock
    private CryptographyToggle cryptographyToggle;
    @InjectMocks
    private UserService userService;

    @Test
    void createMessage_userNotFound() throws Exception {
        when(accountRepo.findByUsername("Spooky Ghost")).thenReturn(Optional.empty());

        CreateMessageResponse result = userService.createMessage("Spooky Ghost", "Bah!".toCharArray(), "password".toCharArray());

        assertNull(result);
        verify(messageRepo, never()).save(any(Message.class));
    }

    @Test
    void createMessage_wrongPassword() throws Exception {
        Account trialAccount = new Account("Patrick Star", new byte[]{1}, new byte[]{2}, new byte[]{3}, "GCM");

        when(accountRepo.findByUsername("Patrick Star")).thenReturn(Optional.of(trialAccount));
        when(cryptographyToggle.passwordCheck(any(), any(), any())).thenReturn(false);

        CreateMessageResponse result = userService.createMessage("Patrick Star", "Heeyyy".toCharArray(), "wrongPass".toCharArray());

        assertNull(result);
        verify(messageRepo, never()).save(any(Message.class));
    }

    @Test
    void createMessage_validData() throws Exception {
        Account trialAccount = new Account("Izzeme Mario", new byte[]{1}, new byte[]{2}, new byte[]{3}, "GCM");
        IV_and_Ciphertext mockResult = new IV_and_Ciphertext(new byte[]{10, 11}, new byte[]{99, 100});

        when(accountRepo.findByUsername("Izzeme Mario")).thenReturn(Optional.of(trialAccount));
        when(cryptographyToggle.passwordCheck(any(), any(), any())).thenReturn(true);
        when(cryptographyToggle.encryptionAES(any(), any(), any(), anyString())).thenReturn(mockResult);

        CreateMessageResponse response = userService.createMessage("Izzeme Mario", "Yiieeepiyeahh!".toCharArray(), "password".toCharArray());

        assertNotNull(response);
        assertEquals("GCM", response.cipherMode());
        verify(messageRepo, times(1)).save(any(Message.class));
    }
}