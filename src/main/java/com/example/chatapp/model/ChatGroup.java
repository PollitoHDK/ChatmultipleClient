package com.example.chatapp.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGroup {
    private String groupName;
    private User creator;
    private List<User> members;

    public ChatGroup(String groupName, User creator) {
        this.groupName = groupName;
        this.creator = creator;
        this.members = new ArrayList<>();
        this.members.add(creator);
    }

    public String getGroupName() {
        return groupName;
    }

    public void addUser(User user) {
        members.add(user);
    }

    // Enviar mensaje de texto a todos los miembros del grupo
    public void broadcastMessage(User sender, String message) {
        for (User member : members) {
            member.getOut().write("[" + groupName + "] " + sender.getUsername() + ": " + message);
        }
    }

    // Enviar nota de voz a todos los miembros del grupo
    public void broadcastAudio(User sender, File audioFile, long fileSize) {
        for (User member : members) {
            try {
                member.getOut().write("Nota de voz en [" + groupName + "] de " + sender.getUsername() + ": " + audioFile.getName());
                member.getOut().write((int) fileSize);

                FileInputStream fis = new FileInputStream(audioFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    member.getOut().write(Arrays.toString(buffer), 0, bytesRead);
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
