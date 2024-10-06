package com.example.chatapp.server;

import com.example.chatapp.model.Group;
import com.example.chatapp.model.User;
import com.example.chatapp.util.HistoryManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 5000;
    private List<User> users;
    private List<Group> groups;
    private HistoryManager historyManager;

    public Server() {
        users = new ArrayList<>();
        groups = new ArrayList<>();
        historyManager = new HistoryManager();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión desde " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addUser(User user) {
        users.add(user);
    }

    public synchronized void removeUser(User user) {
        users.remove(user);
        for (Group group : groups) {
            group.removeMember(user);
        }
    }

    public synchronized Group findGroupByName(String groupName) {
        for (Group group : groups) {
            if (group.getGroupName().equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        return null;
    }

    public synchronized void createGroup(String groupName, User creator) {
        if (findGroupByName(groupName) == null) {
            Group newGroup = new Group(groupName);
            newGroup.addMember(creator);
            groups.add(newGroup);
            System.out.println("Grupo '" + groupName + "' creado por " + creator.getUsername());
        }
    }

    public synchronized List<Group> getGroups() {
        return groups;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public synchronized User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }
}