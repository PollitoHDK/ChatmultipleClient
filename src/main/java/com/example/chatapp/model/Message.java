package com.example.chatapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private LocalDateTime timestamp;
    private String content;
    private MessageType type;

    public enum MessageType {
        TEXT,
        AUDIO,
        CALL
    }

    public Message(String sender, String receiver, String content, MessageType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }
}