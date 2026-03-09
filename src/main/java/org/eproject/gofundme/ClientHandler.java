package org.eproject.gofundme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final EventManager eventManager;
    // Stores client address (IP:port) once at construction for consistent logging.
    private final String clientAddress;

    public ClientHandler(Socket socket, EventManager eventManager) {
        this.socket = socket;
        this.eventManager = eventManager;
        this.clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String command;
            // Read commands from the client until the connection closes.
            while ((command = in.readLine()) != null) {
                System.out.println("[REQUEST] Client " + clientAddress + " -> " + command);
                switch (command.trim().toUpperCase()) {
                    case "CREATE" -> handleCreate(in, out);
                    case "LIST"   -> handleList(out);
                    case "DONATE" -> handleDonate(in, out);
                    case "QUIT"   -> {
                        System.out.println("[DISCONNECTED] Client " + clientAddress);
                        return;
                    }
                    default -> out.println("ERROR: Unknown command: " + command + ". Try again.");
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Client " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to close socket for " + clientAddress);
            }
        }
    }

    // Reads event details line by line, validates them, and delegates to EventManager.
    private void handleCreate(BufferedReader in, PrintWriter out) throws IOException {
        String name = in.readLine();
        String targetStr = in.readLine();
        String deadlineStr = in.readLine();

        try {
            double targetAmount = Double.parseDouble(targetStr);
            LocalDate deadline = LocalDate.parse(deadlineStr);

            if (targetAmount <= 0) {
                out.println("ERROR: Target amount must be greater than zero.");
                return;
            }

            eventManager.createEvent(name, targetAmount, deadline);
            out.println("SUCCESS: Event \"" + name + "\" created successfully.");
            System.out.println("[CREATE] Client " + clientAddress + " created event: " + name);
        } catch (NumberFormatException e) {
            out.println("ERROR: Invalid target amount.");
        } catch (DateTimeParseException e) {
            out.println("ERROR: Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    // Fetches and sends both current and past event lists to the client.
    // Sends "END_LIST" as a sentinel so the client knows the list is complete.
    private void handleList(PrintWriter out) {
        List<FundraisingEvent> current = eventManager.getCurrentEvents();
        List<FundraisingEvent> past = eventManager.getPastEvents();

        //Displays all current events with proper numbering.
        out.println("--- Current Events ---");
        if (current.isEmpty()) {
            out.println("  No current events.");
        } else {
            for (int i = 0; i < current.size(); i++) {
                out.println("ACCOUNT ID[" + i + "] " + current.get(i));
            }
        }

        //Displays all past events with proper numbering.
        out.println("--- Past Events ---");
        if (past.isEmpty()) {
            out.println("  No past events.");
        } else {
            for (int i = 0; i < past.size(); i++) {
                out.println("ACCOUNT ID[" + i + "] " + past.get(i));
            }
        }

        // Sentinel line so the client knows the full list has been received.
        out.println("END_OF_LIST");
        System.out.println("[LIST] Client " + clientAddress + " requested event list.");
    }

    // Reads event index and donation amount, then delegates to EventManager.
    private void handleDonate(BufferedReader in, PrintWriter out) throws IOException {
        String indexStr = in.readLine();
        String amountStr = in.readLine();

        try {
            int index = Integer.parseInt(indexStr);
            double amount = Double.parseDouble(amountStr);
            String result = eventManager.donate(index, amount);
            out.println(result);
            System.out.println("[DONATE] Client " + clientAddress + " -> " + result);
        } catch (NumberFormatException e) {
            out.println("ERROR: Invalid index or amount.");
        }
    }
}
