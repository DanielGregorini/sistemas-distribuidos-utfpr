package com.edu.utfpr.client.view;

import com.edu.utfpr.client.ChatClient;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Login extends JFrame {
    private final JTextField username;
    private final JPasswordField passwordField;
    private ChatClient chatClient;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        FlatLightLaf.setup();
        UIManager.setLookAndFeel(new FlatLightLaf());

        Login loginAndRegisterGUI = new Login();
        loginAndRegisterGUI.setVisible(true);
    }

    public Login() {
            setTitle("Bem-vindo ao Sistema");
            setSize(450, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocation(new Point(150, 150));
            setResizable(true);
        
            ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png")));
            Image image = logo.getImage();
            Image resizedImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
        
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImage));
            constraints.gridwidth = 2;
            constraints.gridx = 0;
            constraints.gridy = 0;
            panel.add(logoLabel, constraints);
        
            constraints.gridy = 1;
            panel.add(new JLabel("Nome de Usuário:"), constraints);
        
            username = new JTextField(20);
            constraints.gridy = 2;
            panel.add(username, constraints);
        
            constraints.gridy = 3;
            panel.add(Box.createRigidArea(new Dimension(0, 8)), constraints);
        
            constraints.gridy = 4;
            panel.add(new JLabel("Senha:"), constraints);
        
            passwordField = new JPasswordField(20);
            constraints.gridy = 5;
            panel.add(passwordField, constraints);
        
            constraints.gridy = 6;
            panel.add(Box.createRigidArea(new Dimension(0, 16)), constraints);
        
            JButton loginButton = new JButton("Entrar");
            loginButton.addActionListener(e -> handleLogin());
            constraints.gridy = 7;
            panel.add(loginButton, constraints);
        
            constraints.gridy = 8;
            panel.add(Box.createRigidArea(new Dimension(0, 4)), constraints);
        
            JButton registerButton = new JButton("Cadastrar");
            registerButton.addActionListener(e -> handleRegister());
            constraints.gridy = 9;
            panel.add(registerButton, constraints);
        
            JCheckBox rememberMe = new JCheckBox("Lembrar-me");
            constraints.gridy = 10;
            panel.add(rememberMe, constraints);
        
            add(panel);
    }

    private void handleLogin() {
        try {
            chatClient = new ChatClient(username.getText());
            chatClient.login(username.getText(), new String(passwordField.getPassword()));

            new ChatClientGUI(chatClient);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String userName = username.getText().trim();
        String password = new String(passwordField.getPassword());
        password = password.trim();
        if (!userName.isEmpty() && !password.isEmpty()) {
            try {
                chatClient = new ChatClient(username.getText());
                chatClient.register(username.getText(), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(this, "Criado", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Usuário já existe", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Campos inválidos.", "Erro", JOptionPane.ERROR_MESSAGE);
        }

    }
}
