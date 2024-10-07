package com.example.chatapp.model;

import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.InetAddress;


public class Client {
    public static String serverIp= "127.0.0.1";
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws UnknownHostException {
            int option;
            System.out.println("Ingrese su nombre de usuario");
            String userName=scanner.nextLine();

            while (true) {
                System.out.println("\n--- Menú de Chat ---");
                System.out.println("1. Unirme a un grupo");
                System.out.println("2. Abrir un chat");
                System.out.print("Seleccione una opción: ");
                option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        int port=5000;
                        System.out.println("A que grupo se quiere unir:/n1.Grupo 1 /n2. Grupo 2 /n3. Grupo 3 /n4. Grupo 4 /n5. Grupo 5");
                        int groupPort=scanner.nextInt();
                        joinGroup(userName,port+groupPort);
                    case 2:
                        System.out.print("Ingrese el nombre del usuario o grupo: ");
                        String destinatarioTexto = scanner.nextLine();
                        System.out.print("Escriba su mensaje: ");
                        // Lógica para enviar mensaje de texto
                        break;
                    case 3:
                        System.out.print("Ingrese el nombre del usuario o grupo: ");
                        String destinatarioAudio = scanner.nextLine();
                        System.out.println("Grabando nota de voz para " + destinatarioAudio + "...");
                        // Lógica para enviar nota de voz
                        break;
                    case 4:
                        System.out.print("Ingrese el nombre del usuario o grupo para la llamada: ");
                        String destinatarioLlamada = scanner.nextLine();
                        System.out.println("Realizando llamada a " + destinatarioLlamada + "...");
                        // Lógica para realizar llamada
                        break;
                    case 6:
                        System.out.println("Saliendo del sistema...");
                        System.exit(0); // Finaliza el programa
                    default:
                        System.out.println("Opción no válida, intente de nuevo.");
                }
            }
    }

    public static void joinGroup(String user, int port){
        System.out.println("Que desea hacer: /n1. Mandar mensaje /n2. Mandar audio");
        String option=scanner.nextLine();
        if(option.equals("1")){
            message();
        }else{
            audio();
        }

    }

    public static void audio(){
        try {
            // conexión con el servidor
            Socket socket = new Socket(serverIp, 5000);

            // Definir el formato de audio
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);

            // Optener información del microfono -> conexión con el hardware especifico
            DataLine.Info infoMicrophone = new DataLine.Info(TargetDataLine.class, format);

            // Conexión con el microfono
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(infoMicrophone);

            // abrir el microfono
            microphone.open(format);
            // el microfono empieza a escuchar
            microphone.start();

            // Conexión del audio con el socket
            OutputStream os = socket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);

            // Codificar el audio en bytes
            byte[] buffer = new byte[10240];
            while (true){
                int byteRead = microphone.read(buffer, 0, buffer.length);

                // Enviarlo por el socket
                bos.write(buffer, 0, byteRead);
                bos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static void message(){
        try {
            // creo la conexión con el servidor
            System.out.println("Conectando al servidor ...");
            Socket socket = new Socket(serverIp, 5000);
            System.out.println("Servidor conectado");

            // entrade de información (consola)
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // lee la información que llega del socket
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // envio por el canal de conexión (socket)
            // BufferedWriter writerBF = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String msg = "";
            System.out.println("Escribe un mensaje para el servidor: ");
            while ( (msg = reader.readLine()) != null && !msg.equalsIgnoreCase("exit")){
                writer.println(msg);
                String response = socketReader.readLine();
                System.out.println(response);
            }

            // Cerrar los buffers y los canales
            // para escribir información en la consola
            // y enviarlos por el socket
            reader.close();
            writer.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}