package model;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {

    }
    public static void Audio(){
        try {

            // Definir el puerto por el cual escucha el servidor
            ServerSocket serverSocket = new ServerSocket(5000);

            // Aceptar la conexión con el cliente
            Socket socket = serverSocket.accept();

            // Definir el formato de audio con el que se decodifica
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);

            // Información del parlante
            DataLine.Info infoSpeaker = new DataLine.Info(SourceDataLine.class, format);
            // conexión con el parlante
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(infoSpeaker);

            // Abren el parlante del equipo
            speaker.open(format);
            //
            speaker.start();

            // Establecen la conexión con el socket -> para obtener la información que envia el cleinte
            InputStream io = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(io);

            // Decodifican la información y se la dan al parlante
            byte[] buffer = new byte[10240];
            while (true){

                //Thread.sleep(500);

                int byteRead = bis.read(buffer, 0, buffer.length);

                speaker.write(buffer, 0, byteRead);
                if(byteRead == -1){
                    break;
                }
            }
            speaker.drain();
            speaker.flush();
            speaker.close();


        }catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        // catch (InterruptedException e) {
        //     throw new RuntimeException(e);
        // }
    }


    public static void Messages(){
        ExecutorService pool = Executors.newFixedThreadPool(3);

        try {
            // abre un puerto para la escucha
            ServerSocket serverSocket = new ServerSocket(5000);

            while (true){
                // aceptar la conexión del cliente
                System.out.println("Esperando conexión ...");
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado desde la IP: " + socket.getInetAddress()
                        + " Con el Puerto: " + socket.getPort());

                // delega a un ThreadPool la lectura de mensajes ( solicitud )
                pool.execute(new ClientHandler(socket));
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            pool.shutdown();
        }
    }
}