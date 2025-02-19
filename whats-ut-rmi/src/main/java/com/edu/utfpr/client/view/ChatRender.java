package com.edu.utfpr.client.view;

import com.edu.utfpr.client.ChatClient;

import javax.swing.*;
import java.awt.*;

public class ChatRender extends JPanel {
    private JTextArea textArea;

    public ChatRender(ChatClient chatClient) {
        initComponent(chatClient);

        chatClient.addChangeCurrentChatListener(chat -> {
            textArea.setText("");
            if (chat != null) {
                chat.getMessages().forEach(
                        message -> appendMessage(message.getSender().getName(), message.getContent())
                );
            }
        });

        chatClient.addOnReceiveMessageListener(
                message -> appendMessage(message.getSender().getName(), message.getContent())
        );
    }

    private void initComponent(ChatClient chatClient) {
        setLayout(new BorderLayout());
        String welcomeMessage = """
                Bem-vindo ao WhatsUT!
                Selecione um chat para começar uma conversa.
                Fique à vontade para explorar!
                """;

        textArea = new JTextArea(welcomeMessage, 14, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(new Color(245, 245, 245));
        textArea.setFont(new Font("Verdana", Font.PLAIN, 14));
        textArea.setForeground(new Color(50, 50, 50));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void appendMessage(String sender, String content) {
        textArea.append(String.format("%s: %s%n", sender, content));
    }
}
