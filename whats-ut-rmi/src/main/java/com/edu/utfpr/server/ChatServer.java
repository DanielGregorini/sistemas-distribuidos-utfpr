package com.edu.utfpr.server;

import com.edu.utfpr.client.IChatClient;
import com.edu.utfpr.domain.entities.Chat;
import com.edu.utfpr.domain.entities.Messages;
import com.edu.utfpr.domain.entities.User;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import javax.swing.JPanel;

public class ChatServer extends UnicastRemoteObject implements IChatServer {
    private final Vector<User> users;
    private final Map<String, String> userCredentials = new HashMap<>();
    private final List<Chat> groups = new ArrayList<>();
    private final List<Chat> privateGroups = new ArrayList<>();

    public ChatServer() throws RemoteException {
        super();
        users = new Vector<>(10, 1);
    }

    public static void main(String[] args) {
        startRMIRegistry();
        String hostName = "localhost";
        String serviceName = "GroupChatService";

        if (args.length == 2) {
            hostName = args[0];
            serviceName = args[1];
        }

        try {
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://" + hostName + "/" + serviceName, server);
            System.out.println("Server listening...");
        } catch (Exception e) {
            System.out.println("Application startup error");
        }
    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("Server started");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createChatGroup(String chatName, String createdBy, boolean exitAdminMethodRandom)
        throws RemoteException {
        User creator = users.stream().filter(user -> user.getName().equals(createdBy)).findFirst().orElse(null);

        UUID guid = UUID.randomUUID();

        Chat chat = new Chat(
                guid,
                chatName,
                creator,
                creator,
                true,
                exitAdminMethodRandom);

        groups.add(chat);
        updatePublicGroupList();
        updateChatList(creator);
    }

    @Override
    public void createInviteGroup(String username, Chat entity) throws RemoteException {
        User userParam = users.stream().filter(user -> user.getName().equals(username)).findFirst().orElse(null);
        Chat chat = groups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst().orElse(null);
        Boolean hasInvite = chat.pendingUsers.contains(userParam);
        Boolean isMember = chat.members.contains(userParam);
        if (!hasInvite && !isMember) {
            chat.pendingUsers.add(userParam);
            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);
            updatePublicGroupList();
        }
    }

    @Override
    public void acceptInviteGroup(String username, Chat entity) throws RemoteException {
        Chat chat = groups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst().orElse(null);
        User userParam = chat.pendingUsers.stream().filter(user -> user.getName().equals(username)).findFirst()
                .orElse(null);
        int indexUser = chat.pendingUsers.indexOf(userParam);

        if (userParam != null) {
            chat.pendingUsers.remove(indexUser);
            chat.members.add(userParam);

            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);

            updatePublicGroupList();
            updateChatList(userParam);
        }
    }

    @Override
    public void login(String username, String password, String hostName, String clientServiceName)
            throws RemoteException, MalformedURLException, NotBoundException, Exception {
        if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
            IChatClient nextClient = (IChatClient) Naming.lookup("rmi://" + hostName + "/" + clientServiceName);
            users.addElement(new User(username, nextClient));
            updateUserList();
        } else {
            throw new Exception("Usuário ou senha inválidos");
        }
    }

    @Override
    public void registerUser(String username, String password)
            throws RemoteException, Exception {
        if (userCredentials.containsKey(username)) {
            throw new Exception("Usuário já existe");
        }

        userCredentials.put(username, password);
    }

    @Override
    public void removeUserFromGroup(String username, Chat entity) throws RemoteException {

        Chat chat = groups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst().orElse(null);
        User userParam = chat.members.stream().filter(user -> user.getName().equals(username)).findFirst().orElse(null);
        int indexUser = chat.members.indexOf(userParam);

        if (userParam != null) {
            chat.members.remove(indexUser);

            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);

            updatePublicGroupList();
            updateChatList(userParam);
        }
    }

    @Override
    public void createPrivateChat(String admin, String recipient) throws RemoteException {
        UUID guid = UUID.randomUUID();

        User currentUser = users.stream().filter(user -> user.getName().equals(admin)).findFirst().orElse(null);

        User destinationUser = users.stream().filter(user -> user.getName().equals(recipient)).findFirst().orElse(null);

        Chat chat = new Chat(
                guid,
                currentUser.getName() + " e " + destinationUser.getName(),
                currentUser,
                currentUser,
                false,
                false,
                List.of(currentUser, destinationUser),
                new ArrayList<>(),
                new ArrayList<>());

        privateGroups.add(chat);

        updateChatList(currentUser);
        updateChatList(destinationUser);
    }

    @Override
    public void leaveGroup(String username, Chat entity) throws RemoteException {

        User currentUser = users.stream().filter(user -> user.getName().equals(username)).findFirst().orElse(null);
        Chat chat = groups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst().orElse(null);
        Messages leaveMessage = null;
        Messages newAdminMessage = null;

        boolean isAdmin = false;
        if (chat.admin == currentUser) {
            isAdmin = true;
        }

        if (!isAdmin) {
            int indexUser = chat.members.indexOf(currentUser);
            chat.members.remove(indexUser);
            leaveMessage = new Messages(currentUser, "Saiu do grupo!");
            chat.messages.add(leaveMessage);
            updatePublicGroupList();
            updateChatList(currentUser);
        } else {
            if (chat.exitAdminMethodRandom) {
                if (chat.members.size() > 1) {
                    int indexUser = chat.members.indexOf(currentUser);
                    chat.members.remove(indexUser);
                    chat.admin = chat.members.get(0);
                    leaveMessage = new Messages(currentUser, "Saiu do grupo!");
                    chat.messages.add(leaveMessage);
                    newAdminMessage = new Messages(chat.admin, chat.admin.name + " agora é o admin do grupo!");
                    chat.messages.add(newAdminMessage);
                    updatePublicGroupList();
                    updateChatList(currentUser);
                } else {
                    groups.remove(chat);
                    updatePublicGroupList();
                    updateChatList(currentUser);
                }
            } else {
                groups.remove(chat);
                updatePublicGroupList();
                updateChatList(currentUser);
            }
        }

        for (User groupUser : chat.members) {
            if (groupUser.getClient() != null) {
                if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && leaveMessage != null) {
                    groupUser.getClient().receiveMessage(leaveMessage);
                }
                if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && newAdminMessage != null) {
                    groupUser.getClient().receiveMessage(newAdminMessage);
                }
            }
        }
    }

    private void updateUserList() {
        List<User> currentUsers = getUserList();
        currentUsers.removeIf(user -> user.getClient() == null);
        for (User user : users) {
            try {
                if (user.getClient() != null) {
                    List<User> candidates = new ArrayList<>(currentUsers);
                    candidates.remove(user);
                    user.getClient().updateUserList(candidates);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePublicGroupList() {
        List<User> currentUsers = getUserList();
        currentUsers.removeIf(user -> user.getClient() == null);
        for (User user : users) {
            try {
                if (user.getClient() != null) {
                    user.getClient().updatePublicGroupList(groups);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateChatList(User currentUser) {
        List<Chat> userChats;
        try {
            userChats = getConversations(currentUser.getName());
            currentUser.getClient().updateChatList(userChats);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private List<User> getUserList() {
        List<User> allUsers = new ArrayList<>(users.size());
        allUsers.addAll(users);
        return allUsers;
    }

    @Override
    public List<User> getCurrentUsers() throws RemoteException {
        return getUserList();
    }

    @Override
    public List<Chat> getGroups() throws RemoteException {
        return groups;
    }

    @Override
    public List<Chat> getConversations(String username) throws RemoteException {
        List<Chat> myChats = new ArrayList<>();

        for (Chat chat : groups) {
            boolean userFound = chat.members.stream().anyMatch(member -> member.name.equals(username));
            if (userFound) {
                myChats.add(chat);
            }
        }

        for (Chat chat : privateGroups) {
            boolean userFound = chat.members.stream().anyMatch(member -> member.name.equals(username));
            if (userFound) {
                myChats.add(chat);
            }
        }

        return myChats;
    }

    @Override
    public void sendMessage(String user, Chat entity, String message, JPanel inputPanel) throws RemoteException {
        User currentUser = users.stream().filter(u -> u.name.equals(user)).findFirst().orElse(null);
        Chat chat;
        chat = groups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst()
                .orElse(privateGroups.stream().filter(chats -> chats.getName().equals(entity.name)).findFirst()
                        .orElse(null));

        Messages newMessage;

        if (entity.isGroup) {
            newMessage = new Messages(currentUser, message);
            chat.messages.add(newMessage);
            for (User groupUser : chat.members) {
                if (groupUser.getClient() != null) {
                    if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && newMessage != null) {
                        groupUser.getClient().receiveMessage(newMessage);
                    }
                }
            }
        } else {
            Chat privateGroup = privateGroups.stream()
                    .filter(other -> entity.getChatId().equals(other.getChatId()))
                    .findFirst()
                    .orElse(null);
            newMessage = new Messages(currentUser, message);

            if (privateGroup != null) {
                privateGroup.messages.add(newMessage);
            }

            for (User privateChatUser : privateGroup.members) {
                if (privateChatUser.getClient() != null && privateChatUser.getClient().getCurrentChatId() != null) {
                    if (privateChatUser.getClient().getCurrentChatId().equals(privateGroup.chatId) && newMessage != null) {
                        privateChatUser.getClient().receiveMessage(newMessage);
                    }
                }
            }
        }
    }
}
