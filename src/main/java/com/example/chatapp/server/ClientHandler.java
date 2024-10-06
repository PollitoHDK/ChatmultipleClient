package com.example.chatapp.server;

import com.example.chatapp.model.Group;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private User user;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Configurar flujos de entrada y salida
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Solicitar y registrar nombre de usuario
            out.println("Ingrese su nombre de usuario:");
            String username = in.readLine();
            while (server.getUserByUsername(username) != null) {
                out.println("Nombre de usuario ya existe. Ingrese otro nombre:");
                username = in.readLine();
            }
            user = new User(username, socket);
            server.addUser(user);
            out.println("Bienvenido, " + username + "!");

            // Enviar historial de mensajes privados
            // Aquí podrías implementar el envío de historial si es necesario

            // Escuchar mensajes del cliente
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                processMessage(clientMessage);
            }
        } catch (IOException e) {
            System.out.println("Conexión con " + user.getUsername() + " perdida.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                server.removeUser(user);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        // Protocolo de comandos:
        // Comandos:
        // CREATE_GROUP <groupName>
        // JOIN_GROUP <groupName>
        // SEND_TEXT <receiver> <mensaje>
        // SEND_AUDIO <receiver> <rutaArchivo>
        // CALL <receiver>
        // HISTORY <receiver>
        // EXIT

        try {
            if (message.startsWith("CREATE_GROUP")) {
                String[] parts = message.split(" ", 2);
                String groupName = parts[1];
                server.createGroup(groupName, user);
                out.println("Grupo '" + groupName + "' creado exitosamente.");
            } else if (message.startsWith("JOIN_GROUP")) {
                String[] parts = message.split(" ", 2);
                String groupName = parts[1];
                Group group = server.findGroupByName(groupName);
                if (group != null) {
                    group.addMember(user);
                    out.println("Te has unido al grupo '" + groupName + "'.");
                } else {
                    out.println("El grupo '" + groupName + "' no existe.");
                }
            } else if (message.startsWith("SEND_TEXT")) {
                String[] parts = message.split(" ", 3);
                String receiver = parts[1];
                String msgContent = parts[2];
                sendTextMessage(receiver, msgContent);
            } else if (message.startsWith("SEND_AUDIO")) {
                String[] parts = message.split(" ", 3);
                String receiver = parts[1];
                String audioPath = parts[2];
                sendAudioMessage(receiver, audioPath);
            } else if (message.startsWith("CALL")) {
                String[] parts = message.split(" ", 2);
                String receiver = parts[1];
                initiateCall(receiver);
            } else if (message.startsWith("HISTORY")) {
                String[] parts = message.split(" ", 2);
                String receiver = parts[1];
                sendHistory(receiver);
            } else if (message.equalsIgnoreCase("EXIT")) {
                out.println("Desconectando...");
                socket.close();
            } else {
                out.println("Comando no reconocido.");
            }
        } catch (Exception e) {
            out.println("Error al procesar el comando: " + e.getMessage());
        }
    }

    private void sendTextMessage(String receiver, String content) {
        // Verificar si el receptor es un usuario o un grupo
        User targetUser = server.getUserByUsername(receiver);
        if (targetUser != null) {
            // Enviar mensaje privado
            targetUser.getOut().println("Mensaje de " + user.getUsername() + ": " + content);
            out.println("Mensaje enviado a " + receiver);
            // Guardar en historial
            Message msg = new Message(user.getUsername(), receiver, content, Message.MessageType.TEXT);
            server.getHistoryManager().saveMessage(msg);
        } else {
            // Verificar si es un grupo
            Group group = server.findGroupByName(receiver);
            if (group != null) {
                // Enviar mensaje al grupo
                for (User member : group.getMembers()) {
                    if (!member.getUsername().equals(user.getUsername())) {
                        member.getOut().println("Mensaje de " + user.getUsername() + " al grupo " + group.getGroupName() + ": " + content);
                    }
                }
                out.println("Mensaje enviado al grupo " + group.getGroupName());
                // Guardar en historial
                Message msg = new Message(user.getUsername(), group.getGroupName(), content, Message.MessageType.TEXT);
                server.getHistoryManager().saveMessage(msg);
            } else {
                out.println("El receptor '" + receiver + "' no existe.");
            }
        }
    }

    private void sendAudioMessage(String receiver, String audioPath) {
        User targetUser = server.getUserByUsername(receiver);
        if (targetUser != null) {
            // Enviar notificación de audio
            targetUser.getOut().println("Nota de voz de " + user.getUsername() + ": " + audioPath);

            // Enviar el archivo de audio a través del socket
            try (FileInputStream fis = new FileInputStream(audioPath);
                 DataOutputStream dos = new DataOutputStream(targetUser.getSocket().getOutputStream())) {
                // Enviar el tamaño del archivo primero
                long fileLength = new File(audioPath).length();
                dos.writeLong(fileLength);

                // Leer el archivo en bloques y enviarlo
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                out.println("Error al enviar la nota de voz a " + receiver);
                return;
            }

            out.println("Nota de voz enviada a " + receiver);
            // Guardar en historial
            Message msg = new Message(user.getUsername(), receiver, audioPath, Message.MessageType.AUDIO);
            server.getHistoryManager().saveMessage(msg);
        } else {
            // Verificar si es un grupo
            Group group = server.findGroupByName(receiver);
            if (group != null) {
                for (User member : group.getMembers()) {
                    if (!member.getUsername().equals(user.getUsername())) {
                        member.getOut().println("Nota de voz de " + user.getUsername() + " al grupo " + group.getGroupName() + ": " + audioPath);
                    }
                }
                out.println("Nota de voz enviada al grupo " + group.getGroupName());
                Message msg = new Message(user.getUsername(), group.getGroupName(), audioPath, Message.MessageType.AUDIO);
                server.getHistoryManager().saveMessage(msg);
            } else {
                out.println("El receptor '" + receiver + "' no existe.");
            }
        }
    }


    private void initiateCall(String receiver) {
        // Para simplificar, solo notificaremos al receptor sobre la llamada
        User targetUser = server.getUserByUsername(receiver);
        if (targetUser != null) {
            targetUser.getOut().println("Llamada entrante de " + user.getUsername());
            out.println("Llamada realizada a " + receiver);
            // Aquí podrías implementar una lógica más compleja para manejar llamadas en tiempo real
        } else {
            // Verificar si es un grupo
            Group group = server.findGroupByName(receiver);
            if (group != null) {
                for (User member : group.getMembers()) {
                    if (!member.getUsername().equals(user.getUsername())) {
                        member.getOut().println("Llamada entrante de " + user.getUsername() + " al grupo " + group.getGroupName());
                    }
                }
                out.println("Llamada realizada al grupo " + group.getGroupName());
            } else {
                out.println("El receptor '" + receiver + "' no existe.");
            }
        }
    }

    private void sendHistory(String receiver) {
        List<String> history = server.getHistoryManager().getHistory(receiver);
        if (history.isEmpty()) {
            out.println("No hay historial para '" + receiver + "'.");
        } else {
            out.println("Historial de '" + receiver + "':");
            for (String msg : history) {
                out.println(msg);
            }
        }
    }
}