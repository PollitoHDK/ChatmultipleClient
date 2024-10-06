package com.example.chatapp.util;

import com.example.chatapp.model.Message;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final String HISTORY_DIR = "chat_history";

    public HistoryManager() {
        File dir = new File(HISTORY_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void saveMessage(Message message) {
        String fileName = HISTORY_DIR + "/" + getFileName(message.getReceiver()) + ".txt";
        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(formatMessage(message));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHistory(String receiver) {
        List<String> history = new ArrayList<>();
        String fileName = HISTORY_DIR + "/" + getFileName(receiver) + ".txt";
        File file = new File(fileName);
        if (!file.exists()) {
            return history;
        }
        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }

    private String formatMessage(Message message) {
        return "[" + message.getTimestamp() + "] " + message.getSender() + ": " + message.getContent();
    }

    private String getFileName(String receiver) {
        return receiver.replaceAll("\\s+", "_");
    }
}