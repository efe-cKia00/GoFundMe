package org.eproject.gofundme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("[CONNECTED] Connected to server at " + HOST + ":" + PORT);

            boolean running = true;
            while (running) {
                //Displays the user menu on the client's end,
                //requests a user input via the scanner (sc),
                //and displays the sent choice.
                printMenu();
                String choice = sc.nextLine().trim();
                System.out.println("[SENT] Menu choice: " + choice);

                switch (choice) {
                    case "1" -> sendCreate(sc, in, out);
                    case "2" -> sendList(in, out);
                    case "3" -> sendDonate(sc, in, out);
                    case "4" -> {
                        out.println("QUIT");
                        System.out.println("[DISCONNECTED] Disconnected from server.");
                        running = false;
                    }
                    default -> System.out.println("[ERROR] Invalid option. Please enter either a 1, 2, 3, or 4.");
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not connect to server: " + e.getMessage());
        }
    }

    // Print Menu method in a Human-readable format.
    private static void printMenu() {
        System.out.println("\n====== GoFundMe Menu ======");
        System.out.println("1. Create Event");
        System.out.println("2. List Events");
        System.out.println("3. Donate");
        System.out.println("4. Quit");
        System.out.print("Choose an option: ");
    }

    // Prompts user for event details, validates on client end, then sends CREATE to the server.
    private static void sendCreate(Scanner sc, BufferedReader in, PrintWriter out) throws IOException {
        System.out.print("Enter event name: ");
        String name = sc.nextLine().trim();

        // Checks to ensure the event name is not an empty string
        // If name is an empty string, then return to while loop inside main class method.
        if (name.isEmpty()) {
            System.out.println("[ERROR] Event name cannot be empty.");
            return;
        }

        // Tries to parse a client's input as a double data type.
        // Uses an if-statement to ensure client's input is greater than 0. If true it jumps to the beginning of while loop (skipping all preceding statements). 
        // Breaks out of while loop if client's input was parseable (double data type) and was greater than zero.
        // Catches any exception if unable to parse into double data type. 
        double targetAmount;
        while (true) {
            System.out.print("Enter target amount: $");
            try {
                targetAmount = Double.parseDouble(sc.nextLine().trim());
                if (targetAmount <= 0) {
                    System.out.println("[ERROR] Target amount must be greater than zero.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid amount. Please enter a number.");
            }
        }

        // Tries to parse a client's input as a LocalDate data type.
        // Uses an if-statement to ensure the parsed date is a future date from the present date. . If true it jumps to the beginning of while loop (skipping all preceding statements).
        // Breaks out of while loop if client's input was parseable (LocalDate data type) and was a present date.
        // Catches any exception if unable to parse into LocalDate data type.
        LocalDate deadline;
        while (true) {
            System.out.print("Enter deadline (YYYY-MM-DD): ");
            try {
                deadline = LocalDate.parse(sc.nextLine().trim());
                if (!deadline.isAfter(LocalDate.now())) {
                    System.out.println("[ERROR] Deadline must be a future date.");
                    continue;
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("[ERROR] Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        // The client uses its "out" socketOutputStream to send a "CREATE" message to the server.
        // The client then sends the client-side verified event name, target amount, and deadline to the server via the socketOutputStream.
        // The client then displays all data sent to server in Human-readable structure.
        out.println("CREATE");
        out.println(name);
        out.println(targetAmount);
        out.println(deadline);
        System.out.println("[SENT] CREATE | " + name + " | $" + targetAmount + " | " + deadline);

        // The client reads the server's response and saves it to a variable.
        // The client then displays the saved response in a Human-readable structure.
        String response = in.readLine();
        System.out.println("[SERVER] " + response);
    }

    // Sends LIST and prints all lines from the server until the END_OF_LIST sentinel.
    private static void sendList(BufferedReader in, PrintWriter out) throws IOException {
        // The client uses its "out" socketOutputStream to sends "LIST" message to the server.
        // The client displays that it sent the "LIST" message to server in Human-readable structure.
        out.println("LIST");
        System.out.println("[SENT] LIST");

        String line;
        while ((line = in.readLine()) != null && !line.equals("END_OF_LIST")) {
            System.out.println(line);
        }
    }

    // Shows the event list first, then prompts for index and amount before sending DONATE.
    private static void sendDonate(Scanner sc, BufferedReader in, PrintWriter out) throws IOException {
        // Display current events so the user can choose an index.
        sendList(in, out);

        // Tries to parse the client's input as an integer (representing an account id for a particular fundraising event).
        // Uses an if statements to ensure the client's input is not less than 0. If true, continues from the beginning of the while loop.
        // Breaks out of while loop if client's input was parceable (int data type) and was greater-or-equal to 0.
        // Catches and explains thrown exceptions in Human-readable format.
        int index;
        while (true) {
            System.out.print("Enter event account id [number] to donate to: ");
            try {
                index = Integer.parseInt(sc.nextLine().trim());
                if (index < 0) {
                    System.out.println("[ERROR] Index cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid account id. Please enter a valid account number.");
            }
        }

        // Tries to parse the client's input as a double (representing the donation amount for a particular fundraising event).
        // Uses an if statements to ensure the client's input is not less than or equal to 0. If true, continues from the beginning of the while loop.
        // Breaks out of while loop if client's input was parceable (double data type) and was greater than 0.
        // Catches and explains thrown exceptions in Human-readable format.
        double amount;
        while (true) {
            System.out.print("Enter donation amount: $");
            try {
                amount = Double.parseDouble(sc.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("[ERROR] Donation amount must be greater than zero.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid amount. Please enter a number.");
            }
        }

        // The client uses its "out" socketOutputStream to send a "DONATE" message to the server.
        // The client then sends the client-side verified event account number (index) and donation amount to the server via the socketOutputStream.
        // The client then displays all data sent to server in a Human-readable format.
        out.println("DONATE");
        out.println(index);
        out.println(amount);
        System.out.println("[SENT] DONATE | index=" + index + " | amount=$" + amount);

        // The client reads the server's response and saves it to a variable.
        // The client then displays the saved response in a Human-readable structure.
        String response = in.readLine();
        System.out.println("[SERVER] " + response);
    }
}
