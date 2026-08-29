package com.example.encryptMsg.repository;

import com.example.encryptMsg.model.Account;
import com.example.encryptMsg.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Integer> {
    List<Message> findAllByAccount(Account account);
}
