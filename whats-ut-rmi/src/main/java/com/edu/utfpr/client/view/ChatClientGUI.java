package com.edu.utfpr.client.view;

import javax.swing.*;

import com.edu.utfpr.client.ChatClient;
import com.edu.utfpr.domain.entities.Chat;
import com.edu.utfpr.domain.entities.User;

import java.awt.*;
import java.rmi.RemoteException;

public class ChatClientGUI extends JFrame {
    protected final JFrame frame;
    private final ChatClient chatClient;
    protected JButton createGroupButton;
    private JTextField textField;

    public ChatClientGUI(ChatClient chatClient) throws RemoteException {
        this.chatClient = chatClient;

        frame = new JFrame("WhatsUT - " + chatClient.userName);

        Container container = getContentPane();
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel sidePanel = buildChatTabs();

        mainPanel.add(buildChatRender(), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        mainPanel.add(buildMessageInput(), BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(buildTopButtons(), BorderLayout.NORTH);

        container.setLayout(new BorderLayout());
        container.add(mainPanel, BorderLayout.CENTER);
        container.add(sidePanel, BorderLayout.WEST);

        frame.add(container);
        frame.setSize(1200, 500);
        frame.setAlwaysOnTop(false);
        frame.setLocationRelativeTo(null);
        textField.requestFocus();

        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel buildChatRender() {
        return new ChatRender(chatClient);
    }

    private JPanel buildChatTabs() throws RemoteException {
        return new ChatTabs(chatClient);
    }
    
    private JPanel buildTopButtons() {
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnListMembers = new JButton("Listar Membros");
        JButton btnLeaveChat = new JButton("Sair do Chat");
        JButton btnPendingInvites = new JButton("Convites Pendentes");
        JButton btnAcceptInvite = new JButton("Aceitar Convite");
        JButton btnDeclineInvite = new JButton("Banir Usuário");
    
        btnListMembers.addActionListener(e -> listMembers(chatClient));
        btnLeaveChat.addActionListener(e -> leaveChat(chatClient));
        btnPendingInvites.addActionListener(e -> showPendingInvites(chatClient));
        btnAcceptInvite.addActionListener(e -> acceptInvite(chatClient));
        btnDeclineInvite.addActionListener(e -> banUser(chatClient));
    
        buttonPanel.add(btnListMembers);
        buttonPanel.add(btnLeaveChat);
        buttonPanel.add(btnPendingInvites);
        buttonPanel.add(btnAcceptInvite);
        buttonPanel.add(btnDeclineInvite);
    
        JPanel createGroupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createGroupPanel.add(buildCreateGroupButton());
    
        topPanel.add(buttonPanel);
        topPanel.add(createGroupPanel);
    
        return topPanel;
    }
    
    private JButton buildCreateGroupButton() {
        createGroupButton = new JButton("+ Novo Grupo");
        createGroupButton.addActionListener(e -> {
            GroupModal newGroupDialog = new GroupModal();
            newGroupDialog.openCreateGroupDialog(chatClient);
        });
        createGroupButton.setEnabled(true);
        createGroupButton.setBackground(Color.LIGHT_GRAY);
        createGroupButton.setFont(new Font("Arial", Font.BOLD, 14));
        return createGroupButton;
    }

    private JPanel buildMessageInput() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        textField = new JTextField();
        textField.setFont(new Font("Verdana", Font.PLAIN, 12));
        textField.addActionListener(e -> handleUserInput(inputPanel));
        inputPanel.add(textField);
        return inputPanel;
    }

    private void handleUserInput(JPanel inputPanel) {
        if (chatClient.currentChat == null) {
            return;
        }
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            try {
                chatClient.sendMessage(message, inputPanel);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } finally {
                textField.setText("");
            }
        }
    }
    
    private void listMembers(ChatClient chatClient) {
        System.out.println("Listando membros...");
        if (chatClient.currentChat == null || !chatClient.currentChat.isGroup) {
            return;
        }
        JDialog dialog = createDialog("Membros do Grupo", chatClient.currentChat.getMembers()
            .stream()
            .map(user -> user.getName())
            .toArray(String[]::new));
        dialog.setVisible(true);
    }

    private void leaveChat(ChatClient chatClient) {
        Chat currentChat = chatClient.currentChat;
        if (currentChat == null || !currentChat.isGroup) {
            return;
        }

        JDialog dialogExit = new JDialog((Frame) null, true);
        dialogExit.setTitle("Sair do grupo " + currentChat.name + " ?");
        dialogExit.setAlwaysOnTop(true);
        dialogExit.setSize(300, 300);
        JPanel panelExit = new JPanel();
        panelExit.setLayout(new GridLayout());
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            try {
                chatClient.leaveGroup(chatClient.currentChat);
                dialogExit.dispose();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            dialogExit.dispose();
        });

        panelExit.add(confirmButton);
        panelExit.add(cancelButton);
        dialogExit.add(panelExit, BorderLayout.NORTH);
        dialogExit.setLocationRelativeTo(null);
        dialogExit.setVisible(true);

        try {
            chatClient.setCurrentChat(null);
            chatClient.leaveGroup(chatClient.currentChat);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void acceptInvite(ChatClient chatClient) {
        System.out.println("Aceitando convite...");
        if (chatClient.currentChat == null || !chatClient.currentChat.isGroup) {
            return;
        }
        User admin = chatClient.currentChat.getAdmin();
        if (admin.getName().equals(chatClient.userName)) {
            try {
                JDialog dialog = createDialog("Convites Pendentes", chatClient.currentChat.getPendingUsers()
                    .stream()
                    .map(user -> user.getName())
                    .toArray(String[]::new));
                dialog.setVisible(true);
                String userName = JOptionPane.showInputDialog(
                    frame,
                    "Digite o nome do usuário que deseja adicionar:",
                    "Adicionar Usuário",
                    JOptionPane.QUESTION_MESSAGE
                );
                chatClient.acceptInvite(chatClient.currentChat, userName);
                
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(
                frame,
                "Você não é o administrador do grupo.",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void banUser(ChatClient chatClient) {
        System.out.println("Banindo usuário...");
        if (chatClient.currentChat == null || !chatClient.currentChat.isGroup) {
            return;
        }
        User admin = chatClient.currentChat.getAdmin();
        if (admin.getName().equals(chatClient.userName)) {
            try {
                JDialog dialog = createDialog("Membros do Grupo", chatClient.currentChat.getMembers()
                    .stream()
                    .map(user -> user.getName())
                    .toArray(String[]::new));
                dialog.setVisible(true);
                String userName = JOptionPane.showInputDialog(
                    frame,
                    "Digite o nome do usuário que deseja banir:",
                    "Banir Usuário",
                    JOptionPane.QUESTION_MESSAGE
                );
                chatClient.removeUserFromGroup(chatClient.currentChat, userName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(
                frame,
                "Você não é o administrador do grupo.",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showPendingInvites(ChatClient chatClient) {
        System.out.println("Mostrando convites pendentes...");
        if (chatClient.currentChat == null || !chatClient.currentChat.isGroup) {
            return;
        }
        User admin = chatClient.currentChat.getAdmin();
        if (admin.getName().equals(chatClient.userName)) {
            JDialog dialog = createDialog("Convites Pendentes", chatClient.currentChat.getPendingUsers()
                .stream()
                .map(user -> user.getName())
                .toArray(String[]::new));
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                frame,
                "Você não é o administrador do grupo.",
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JDialog createDialog(String title, String[] items) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setTitle(title);
        dialog.setAlwaysOnTop(true);
        dialog.setSize(200, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(items.length, 1));
        for (String item : items) {
            JLabel label = new JLabel(item);
            panel.add(label);
        }
        dialog.add(panel, BorderLayout.NORTH);
        dialog.setLocationRelativeTo(null);
        return dialog;
    }
}
