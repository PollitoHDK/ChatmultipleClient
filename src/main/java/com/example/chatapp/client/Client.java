package com.example.chatapp.client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private String username;

    public Client() {
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Conectado al servidor.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            registerUser();

            new Thread(new ServerListener()).start();

            showMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerUser() throws IOException {
        System.out.print(in.readLine() + " ");
        username = scanner.nextLine();
        out.println(username);
        String response = in.readLine();
        while (response.startsWith("Nombre de usuario ya existe")) {
            System.out.print(response + " ");
            username = scanner.nextLine();
            out.println(username);
            response = in.readLine();
        }
        System.out.println(response);
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n--- Menú de Chat ---");
            System.out.println("1. Crear grupo");
            System.out.println("2. Unirse a un grupo");
            System.out.println("3. Iniciar chat");
            System.out.println("6. Ver historial");
            System.out.println("9. Salir");
            System.out.print("Seleccione una opción: ");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    createGroup();
                    break;
                case 2:
                    joinGroup();
                    break;
                case 3:
                    sendTextMessage();
                    break;
                case 4:
                    chatMenu();
                    break;
                case 9:
                    exit();
                    return;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void chatMenu(){
        int option = 0;

        while (option != 9) {
            System.out.println("\n--- Menú de Chat ---");
            System.out.println("1. Enviar mensaje de texto");
            System.out.println("2. Enviar nota de voz");
            System.out.println("3. Realizar llamada");
            System.out.println("9. Salir");
            System.out.print("Seleccione una opción: ");
            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    sendTextMessage();
                    break;
                case 2:
                    sendVoiceNote();
                    break;
                case 3:
                    makeCall();
                    break;
                case 6:
                    viewHistory();
                    break;
                case 9:
                    System.out.println("Saliendo del chat...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void createGroup() {
        System.out.print("Ingrese el nombre del grupo: ");
        String groupName = scanner.nextLine();
        out.println("CREATE_GROUP " + groupName);
    }

    private void joinGroup() {
        System.out.print("Ingrese el nombre del grupo al que desea unirse: ");
        String groupName = scanner.nextLine();
        out.println("JOIN_GROUP " + groupName);
    }

    private void sendTextMessage() {
        System.out.print("Ingrese el nombre del usuario o grupo destinatario: ");
        String receiver = scanner.nextLine();
        System.out.print("Ingrese el mensaje: ");
        String message = scanner.nextLine();
        out.println("SEND_TEXT " + receiver + " " + message);
    }

    private void sendVoiceNote() {
        System.out.print("Ingrese el nombre del usuario o grupo destinatario: ");
        String receiver = scanner.nextLine();
        System.out.println("Grabando nota de voz... Presione ENTER para detener.");
        String audioFilePath = "audio_" + System.currentTimeMillis() + ".wav";
        recordAudio(audioFilePath);

        try {
            // Leer el archivo de audio y enviar su contenido
            File audioFile = new File(audioFilePath);
            FileInputStream fis = new FileInputStream(audioFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Enviar el encabezado con el nombre del destinatario y el tamaño del archivo
            out.println("SEND_AUDIO " + receiver + " " + audioFile.getName() + " " + audioFile.length());

            // Enviar los bytes del archivo
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Asegúrate de que 'out' pueda enviar bytes (podría ser un PrintWriter o similar)
                out.write(Arrays.toString(buffer), 0, bytesRead);  // Este es un método que podría variar según tu implementación de socket
            }

            fis.close();
            System.out.println("Nota de voz enviada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void recordAudio(String filePath) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Línea de audio no soportada.");
                return;
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            AudioInputStream ais = new AudioInputStream(line);
            File audioFile = new File(filePath);
            Thread stopper = new Thread(() -> {
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                line.stop();
                line.close();
                System.out.println("Grabación detenida.");
            });
            stopper.start();

            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void makeCall() {
        System.out.print("Ingrese el nombre del usuario o grupo a llamar: ");
        String receiver = scanner.nextLine();
        out.println("CALL " + receiver);
    }

    private void viewHistory() {
        System.out.print("Ingrese el nombre del usuario o grupo para ver el historial: ");
        String receiver = scanner.nextLine();
        out.println("HISTORY " + receiver);
    }

    private void exit() {
        out.println("EXIT");
        System.out.println("Desconectando del servidor...");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("Nota de voz de")) {
                        // Extraer la ruta del archivo de audio
                        String[] parts = serverMessage.split(": ");
                        if (parts.length == 2) {
                            String audioPath = parts[1];
                            System.out.println(serverMessage);
                            System.out.print("¿Desea reproducir la nota de voz? (s/n): ");
                            String choice = scanner.nextLine();
                            if (choice.equalsIgnoreCase("s")) {
                                playAudio(audioPath);
                            }
                        }
                    } else if (serverMessage.startsWith("Llamada entrante")) {
                        System.out.println(serverMessage);
                        System.out.print("¿Desea responder la llamada? (s/n): ");
                        String choice = scanner.nextLine();
                        if (choice.equalsIgnoreCase("s")) {
                            System.out.println("Llamada respondida.");
                            // Aquí podrías implementar la lógica de la llamada
                        } else {
                            System.out.println("Llamada rechazada.");
                        }
                    } else {
                        System.out.println(serverMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Conexión con el servidor perdida.");
            }
        }
    }

    private void playAudio(String audioPath) {
        try {
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                System.out.println("Archivo de audio no encontrado: " + audioPath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Formato de audio no soportado.");
                audioStream.close();
                return;
            }

            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            System.out.println("Reproduciendo nota de voz...");

            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = audioStream.read(buffer)) != -1) {
                audioLine.write(buffer, 0, bytesRead);
            }

            audioLine.drain();
            audioLine.close();
            audioStream.close();

            System.out.println("Reproducción finalizada.");
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }
}