package com.edu.utfpr.client.view;

import com.edu.utfpr.client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.RemoteException;

public class GroupModal {

    public void openCreateGroupDialog(ChatClient chatClient) {
        JDialog dialog = new JDialog((Frame) null, "Criar Novo Grupo", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new BorderLayout());
        dialog.setAlwaysOnTop(true);

        JPanel formPanel = createFormPanel();
        JTextField nameField = (JTextField) formPanel.getClientProperty("nameField");
        JRadioButton radioAdminReplacement = (JRadioButton) formPanel.getClientProperty("radioAdminReplacement");

        JPanel buttonPanel = createButtonPanel(dialog, chatClient, nameField, radioAdminReplacement);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel nameLabel = new JLabel("Nome do Grupo:");
        JTextField nameField = new JTextField(20);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JRadioButton radioAdminReplacement = new JRadioButton("Quando admin sair, escolher outro admin automaticamente.");
        JRadioButton radioGroupDeletion = new JRadioButton("Quando admin sair, excluir o grupo.");
        radioGroupDeletion.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioAdminReplacement);
        buttonGroup.add(radioGroupDeletion);

        formPanel.add(nameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(radioAdminReplacement);
        formPanel.add(radioGroupDeletion);

        formPanel.putClientProperty("nameField", nameField);
        formPanel.putClientProperty("radioAdminReplacement", radioAdminReplacement);

        return formPanel;
    }

    private JPanel createButtonPanel(JDialog dialog, ChatClient chatClient, JTextField nameField, JRadioButton radioAdminReplacement) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton createButton = new JButton("Criar");
        JButton cancelButton = new JButton("Cancelar");

        createButton.addActionListener(e -> handleCreateGroup(dialog, chatClient, nameField, radioAdminReplacement));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void handleCreateGroup(JDialog dialog, ChatClient chatClient, JTextField nameField, JRadioButton radioAdminReplacement) {
        String groupName = nameField.getText().trim();
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "O nome do grupo não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            chatClient.createChatGroup(groupName, chatClient.userName, radioAdminReplacement.isSelected());
            JOptionPane.showMessageDialog(dialog, "Grupo " + groupName + " criado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        } catch (RemoteException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Erro ao criar grupo!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
