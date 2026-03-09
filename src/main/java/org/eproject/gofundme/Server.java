package org.eproject.gofundme;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        // Single shared EventManager instance passed to all ClientHandler threads.
        EventManager eventManager = new EventManager();

        System.out.println("Server started. Listening on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Blocks until a client connection is received.
                Socket clientSocket = serverSocket.accept();

                // Saves the client's IP address and Port number to a variable to be printed out on the server.
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();
                System.out.println("[CONNECTED] Client " + clientIP + ":" + clientPort);

                // Spawn a new thread for each connected client.
                Thread clientThread = new Thread(new ClientHandler(clientSocket, eventManager));
                clientThread.start();
            }
        } catch (IOException e) {
            // Show any/all server IOException error message(s).
            System.out.println("[ERROR] Server encountered an error: " + e.getMessage());
        }
    }
}
