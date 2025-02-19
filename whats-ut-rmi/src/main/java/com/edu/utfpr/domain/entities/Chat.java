package com.edu.utfpr.domain.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat implements Serializable {
    public UUID chatId;
    public String name;
    public User admin;
    public User created_by;
    public Boolean isGroup;
    public Boolean exitAdminMethodRandom;

    public List<User> members = new ArrayList<>();
    public List<User> pendingUsers = new ArrayList<>();
    public List<Messages> messages = new ArrayList<>();

    public Chat(UUID chatId, String name, User _admin, User created_by, Boolean isGroup,
            Boolean exitAdminMethodRandom) {
        this.chatId = chatId;
        this.name = name;
        this.admin = _admin;
        this.created_by = created_by;
        this.isGroup = isGroup;
        this.exitAdminMethodRandom = exitAdminMethodRandom;

        initializeDefaultMessage(created_by);
        this.members.add(created_by);
    }

    private void initializeDefaultMessage(User createdBy) {
        Messages firstMessage = new Messages(createdBy, "Digite no campo de texto e pressione Enter para enviar");
        this.messages.add(firstMessage);
    }

    public void addMessage(Messages message) {
        this.messages.add(message);
    }

    public void addMember(User user) {
        this.members.add(user);
    }
}
