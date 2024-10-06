package com.example.chatapp.model;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private String username;
    private Socket socket;
    private PrintWriter out;

    public User(String username, Socket socket) throws Exception {
        this.username = username;
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getOut() {
        return out;
    }
}
