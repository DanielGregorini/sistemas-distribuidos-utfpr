package com.edu.utfpr.client.view;

import com.edu.utfpr.client.ChatClient;
import com.edu.utfpr.domain.entities.Chat;
import com.edu.utfpr.domain.entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.List;

public class ChatTabs extends JPanel {
    private final ChatClient chatClient;
    private JTabbedPane tabbedPane;
    private DefaultListModel<Chat> myChatsModel, groupsModel;
    private DefaultListModel<User> allUsersModel;

    public ChatTabs(ChatClient chatClient) throws RemoteException {
        this.chatClient = chatClient;

        List<User> currentOnlineUsers = chatClient
                .getCurrentUsers()
                .stream()
                .filter(user -> !user.getName().equals(chatClient.userName))
                .toList();

        initComponents(currentOnlineUsers, chatClient.getGroups(), chatClient.getConversations());
    }

    private void initComponents(List<User> currentOnlineUsers, List<Chat> groups, List<Chat> myChats) {
        tabbedPane = new JTabbedPane();

        setupMyChatsTab();
        setupUsersTab();
        setupGroupsTab();

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        setupListeners();
        updateLists(currentOnlineUsers, groups, myChats);
    }

    private void setupMyChatsTab() {
        myChatsModel = new DefaultListModel<>();
        JList<Chat> myChatsList = createChatList(myChatsModel, "Conversas");
        myChatsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Chat chat = myChatsList.getSelectedValue();
                    if (chat != null) setCurrentChat(chat);
                }
            }
        });
    }

    private void setupUsersTab() {
        allUsersModel = new DefaultListModel<>();
        JList<User> allUsersList = createUserList(allUsersModel, "Usuários online");
        allUsersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    User user = allUsersList.getSelectedValue();
                    if (user != null) createPrivateChat(user);
                }
            }
        });
    }

    private void setupGroupsTab() {
        groupsModel = new DefaultListModel<>();
        JList<Chat> groupsList = createChatList(groupsModel, "Grupos");
        groupsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Chat chat = groupsList.getSelectedValue();
                    if (chat == null) return;
                    if (chat.getMembers().stream().anyMatch(user -> user.getName().equals(chatClient.userName))) {
                        setCurrentChat(chat);
                    } else {
                        sendGroupJoinRequest(chat);
                    }
                }
            }
        });
    }

    private JList<Chat> createChatList(DefaultListModel<Chat> model, String tabTitle) {
        JList<Chat> list = new JList<>(model);
        list.setCellRenderer(createChatListsCellRenderer());
        addTab(tabTitle, list);
        return list;
    }

    private JList<User> createUserList(DefaultListModel<User> model, String tabTitle) {
        JList<User> list = new JList<>(model);
        list.setCellRenderer(createUsersListsCellRenderer());
        addTab(tabTitle, list);
        return list;
    }

    private void addTab(String title, JList<?> list) {
        JScrollPane scrollPane = new JScrollPane(list);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.addTab(title, panel);
    }

    private void setCurrentChat(Chat chat) {
        try {
            chatClient.setCurrentChat(chat);
            tabbedPane.setSelectedIndex(0);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void createPrivateChat(User user) {
        try {
            List<Chat> myChats = chatClient.getConversations();
            for (Chat chat : myChats) {
                if (chat.getMembers().size() == 2 && chat.getMembers().contains(user)) {
                    setCurrentChat(chat);
                    return;
                }
            }
            chatClient.createPrivateChat(user.getName());
            tabbedPane.setSelectedIndex(0);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void sendGroupJoinRequest(Chat chat) {
        try {
            chatClient.sendInviteAdmin(chatClient.userName, chat);
            JOptionPane.showMessageDialog(tabbedPane,
                    "Seu pedido para entrar no grupo foi enviado. Aguarde até que o administrador aceite sua entrada.",
                    "Pedido enviado",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(tabbedPane, "Erro ao enviar pedido.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ListCellRenderer<? super User> createUsersListsCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((User) value).getName());
                return this;
            }
        };
    }

    private ListCellRenderer<? super Chat> createChatListsCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((Chat) value).getName());
                return this;
            }
        };
    }

    private void setupListeners() {
        chatClient.addChangeUserListListener(this::onChangeUserList);
        chatClient.addChangeGroupListListener(this::onChangeGroupList);
        chatClient.addChangeMyChatsListListener(this::onChangeMyChatsList);
    }

    private void updateLists(List<User> currentOnlineUsers, List<Chat> groups, List<Chat> myChats) {
        onChangeUserList(currentOnlineUsers);
        onChangeGroupList(groups);
        onChangeMyChatsList(myChats);
    }

    private void onChangeUserList(List<User> onlineUsers) {
        allUsersModel.clear();
        onlineUsers.forEach(allUsersModel::addElement);
    }

    private void onChangeGroupList(List<Chat> groups) {
        groupsModel.clear();
        groups.forEach(groupsModel::addElement);
    }

    private void onChangeMyChatsList(List<Chat> myChats) {
        myChatsModel.clear();
        myChats.forEach(myChatsModel::addElement);
    }
}
