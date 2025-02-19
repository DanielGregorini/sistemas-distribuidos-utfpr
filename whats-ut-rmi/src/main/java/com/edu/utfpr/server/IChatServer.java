package com.edu.utfpr.server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JPanel;

import com.edu.utfpr.domain.entities.Chat;
import com.edu.utfpr.domain.entities.User;

public interface IChatServer extends Remote {
        List<User> getCurrentUsers() throws RemoteException;
        List<Chat> getGroups() throws RemoteException;
        List<Chat> getConversations(String userName) throws RemoteException;


        void createChatGroup(String chatName, String creator, boolean exitAdminMethod) throws RemoteException;
        void createPrivateChat(String user1, String user2) throws RemoteException;
        void sendMessage(String user, Chat chat, String message, JPanel inputPanel) throws RemoteException;

        void registerUser(String userName, String password) throws RemoteException, Exception;
        void login(String userName, String password, String hostName, String clientServiceName) throws RemoteException, MalformedURLException, NotBoundException, Exception;
        
        void createInviteGroup(String userName, Chat chat) throws RemoteException;
        void acceptInviteGroup(String userName, Chat chat) throws RemoteException;
        void removeUserFromGroup(String userName, Chat chatParam) throws RemoteException;
        void leaveGroup(String UserName, Chat chat) throws RemoteException;
        
}
