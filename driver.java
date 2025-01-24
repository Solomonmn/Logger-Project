import java.io.*;
import java.util.*;

public class driver {
    private static Scanner scanner = new Scanner(System.in);
    private static List<String> history = new ArrayList<>();

    // Processes for Logger and Encryption Program
    private static Process loggerProcess;
    private static Process encryptionProcess;
    private static BufferedWriter loggerWriter;
    private static BufferedReader encryptionReader;
    private static BufferedWriter encryptionWriter;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java driver <log_file>");
            System.exit(1);
        }

        String logFileName = args[0];

        try {
            // Start of using the Logger Process
            ProcessBuilder loggerBuilder = new ProcessBuilder("java", "Logger", logFileName);
            loggerBuilder.redirectErrorStream(true); 
            loggerProcess = loggerBuilder.start();
            loggerWriter = new BufferedWriter(new OutputStreamWriter(loggerProcess.getOutputStream()));

            // Start of using the Encryption Program Process
            ProcessBuilder encryptionBuilder = new ProcessBuilder("java", "encrypt");
            encryptionBuilder.redirectErrorStream(true); 
            encryptionProcess = encryptionBuilder.start();
            encryptionWriter = new BufferedWriter(new OutputStreamWriter(encryptionProcess.getOutputStream()));
            encryptionReader = new BufferedReader(new InputStreamReader(encryptionProcess.getInputStream()));

            // Log start of the Driver Program
            log("START", "Logging Started.");

            boolean running = true;
            while (running) {
                displayMenu();
                String command = scanner.nextLine().trim().toLowerCase();

                switch (command) {
                    case "password":
                        handlePassword();
                        break;
                    case "encrypt":
                        handleEncrypt();
                        break;
                    case "decrypt":
                        handleDecrypt(); 
                        break;
                    case "history":
                        displayHistory(false); 
                        promptContinue();
                        break;
                    case "quit":
                        running = false;
                        handleQuit();
                        break;
                    default:
                        System.out.println("!!ERROR: Invalid command. Please try again");
                        log("INVALID_COMMAND", "Invalid command entered: " + command);
                        promptContinue();
                }
            }

        } catch (IOException e) {
            System.err.println("!!ERROR: Error starting processes: " + e.getMessage());
            log("ERROR", "Error starting processes: " + e.getMessage());
            System.exit(1);
        }
    }

    // this the main menu
    private static void displayMenu() {
        String line = "---------------------------------------------------------";
        System.out.println(line);
        System.out.println("                          Menu");
        System.out.println(line);
        System.out.println("password - set the password for encryption/decryption");
        System.out.println("encrypt  - encrypt a string");
        System.out.println("decrypt  - decrypt a string");
        System.out.println("history  - show history");
        System.out.println("quit     - quit program");
        System.out.println(line);
        System.out.print("Enter Command: ");
    }

    //logs action to logger
    private static void log(String action, String message) {
        try {
            // Send Message to Logger
            String logEntry = action.toUpperCase() + " " + message;
            loggerWriter.write(logEntry);
            loggerWriter.newLine();
            loggerWriter.flush();
        } catch (IOException e) {
            System.err.println("!!ERROR: Error writing to logger: " + e.getMessage());
        }
    }

    // will handle the passkey
    private static void handlePassword() {
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            // Sends PASSKEY command to Encryption Program using the process
            encryptionWriter.write("PASSKEY " + password);
            encryptionWriter.newLine();
            encryptionWriter.flush();


            String response = encryptionReader.readLine();
            log("SET_PASSWORD", "Setting passkey.");

            if (response == null) {
                System.out.println("!!ERROR: No response from Encryption Program.");
                log("SET_PASSWORD_ERROR", "No response received for PASSKEY.");
                promptContinue();
                return;
            }

            if (response.startsWith("RESULT")) {
                System.out.println("Password set successfully.");
                log("SET_PASSWORD", "Success.");
            } else if (response.startsWith("!!ERROR:")) {
                String errorMsg = response.substring(8).trim();
                System.out.println(errorMsg);
                log("SET_PASSWORD_ERROR", "Error setting password: " + errorMsg);
            } else {
                System.out.println("!!ERROR: Unexpected response from Encryption Program.");
                log("SET_PASSWORD_ERROR", "Unexpected response for PASSKEY: " + response);
            }

            promptContinue();

        } catch (IOException e) {
            System.out.println("!!ERROR: Communication error with Encryption Program: " + e.getMessage());
            log("SET_PASSWORD_ERROR", "Communication error during PASSKEY: " + e.getMessage());
        }
    }

    // this handles the encryption process 
    private static void handleEncrypt() {
        System.out.print("Enter String to ENCRYPT: ");
        String plaintext = scanner.nextLine().trim();

        if (plaintext.isEmpty()) {
            System.out.println("!!ERROR: Input cannot be empty.");
            log("ENCRYPT", "Error: Input cannot be empty.");
            promptContinue();
            return;
        }

        // Send ENCRYPT command using the process communication
        try {
            encryptionWriter.write("ENCRYPT " + plaintext);
            encryptionWriter.newLine();
            encryptionWriter.flush();

            // Read response
            String response = encryptionReader.readLine();
            log("ENCRYPT", plaintext.toUpperCase());

            if (response == null) {
                System.out.println("!!ERROR: No response from Encryption Program.");
                log("ENCRYPT", "Error: No response received.");
                promptContinue();
                return;
            }

            if (response.startsWith("RESULT")) {
                String ciphertext = response.substring(7).trim().toUpperCase(); // Ensure all caps
                System.out.println("\nRESULT: " + ciphertext);
                history.add(plaintext.toUpperCase());
                history.add(ciphertext);
                log("ENCRYPT", "Success: " + ciphertext + ".");
            } else if (response.startsWith("!!ERROR:")) {
                String errorMsg = response.substring(8).trim();
                System.out.println("\n" + errorMsg);
                log("ENCRYPT", "Error: " + errorMsg);
            } else {
                System.out.println("!!ERROR: Unexpected response from Encryption Program.");
                log("ENCRYPT", "Unexpected response for ENCRYPT: " + response);
            }

            promptContinue();

        } catch (IOException e) {
            System.out.println("!!ERROR: Communication error with Encryption Program: " + e.getMessage());
            log("ENCRYPT", "Communication error during ENCRYPT: " + e.getMessage());
        }
    }

     // this handles the decryption process 
    private static void handleDecrypt() {
        // This asks if the user wants to use history
        System.out.print("Would you like to use the history? (Y/N) ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("y")) {
            // User choses to see the history
            displayHistory(true); 
            selectHistoryForDecrypt();
        } else if (choice.equals("n")) {
            // User chose not to use history; proceed to prompt for input
            System.out.print("\nEnter String to DECRYPT: ");
            String ciphertext = scanner.nextLine().trim();

            if (ciphertext.isEmpty()) {
                System.out.println("!!ERROR: Input cannot be empty.");
                log("DECRYPT", "Error decrypting: Input cannot be empty.");
                promptContinue();
                return;
            }

            // Send DECRYPT command using the process communication
            try {
                encryptionWriter.write("DECRYPT " + ciphertext);
                encryptionWriter.newLine();
                encryptionWriter.flush();

                // Read response
                String response = encryptionReader.readLine();
                log("DECRYPT", ciphertext.toUpperCase());

                if (response == null) {
                    System.out.println("!!ERROR: No response from Encryption Program.");
                    log("DECRYPT", "Error decrypting: No response received.");
                    promptContinue();
                    return;
                }

                if (response.startsWith("RESULT")) {
                    String plaintext = response.substring(7).trim().toUpperCase(); // Ensure all caps
                    System.out.println("\nRESULT: " + plaintext);
                    history.add(ciphertext.toUpperCase());
                    history.add(plaintext);
                    log("DECRYPT", "Success: " + plaintext + ".");
                } else if (response.startsWith("!!ERROR:")) {
                    String errorMsg = response.substring(8).trim();
                    System.out.println("\n" + errorMsg);
                    log("DECRYPT", "Error decrypting: " + errorMsg);
                } else {
                    System.out.println("!!ERROR: Unexpected response from Encryption Program.");
                    log("DECRYPT", "Unexpected response for DECRYPT: " + response);
                }

                
                promptContinue();

            } catch (IOException e) {
                System.out.println("!!ERROR: Communication error with Encryption Program: " + e.getMessage());
                log("DECRYPT", "Communication error during DECRYPT: " + e.getMessage());
            }
        } else {
            // Invalid choice; prompt again or handle as needed
            System.out.println("!!ERROR: Invalid choice. Please enter 'Y' or 'N'.");
            log("DECRYPT", "Invalid history choice: " + choice);
            promptContinue();
        }
    }

    // shows the history
    private static void displayHistory(boolean includeGoBack) {
        if (history.isEmpty()) {
            System.out.println("\nHistory is empty.");
        } else {
            System.out.println("\n---------------------------------------------------------");
            System.out.println("                   History");
            System.out.println("---------------------------------------------------------");
            for (int i = 0; i < history.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + history.get(i));
            }
            if (includeGoBack) {
                System.out.println("  " + (history.size() + 1) + ". (Go Back)");
            }
            System.out.println("\n---------------------------------------------------------");
        }
        log("HISTORY", "History Checked.");
    }

    // selecting the string to decrypt from history
    private static void selectHistoryForDecrypt() {
        if (history.isEmpty()) {
            promptContinue();
            return;
        }

        System.out.print("\nSelect String to DECRYPT: ");
        String selection = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(selection);
            if (index == history.size() + 1) {
                
                promptContinue();
                return;
            }
            if (index < 1 || index > history.size()) {
                System.out.println("!!ERROR: Invalid selection.");
                log("HISTORY", "Invalid history selection: " + selection);
                promptContinue();
                return;
            }
            String selected = history.get(index - 1);
            if (selected.isEmpty()) {
                System.out.println("!!ERROR: Selected history entry is empty.");
                log("HISTORY", "Selected history entry is empty.");
                promptContinue();
                return;
            }

            // Decrypt the selected string using the communication via process
            encryptionWriter.write("DECRYPT " + selected);
            encryptionWriter.newLine();
            encryptionWriter.flush();

            String response = encryptionReader.readLine();
            log("DECRYPT", selected.toUpperCase());

            if (response == null) {
                System.out.println("!!ERROR: No response from Encryption Program.");
                log("DECRYPT", "Error decrypting: No response received.");
                promptContinue();
                return;
            }

            if (response.startsWith("RESULT")) {
                String plaintext = response.substring(7).trim().toUpperCase(); // Ensure all caps
                System.out.println("\nRESULT: " + plaintext);
                history.add(selected.toUpperCase());
                history.add(plaintext);
                log("DECRYPT", "Success: " + plaintext + ".");
            } else if (response.startsWith("!!ERROR:")) {
                String errorMsg = response.substring(8).trim();
                System.out.println("\n" + errorMsg);
                log("DECRYPT", "Error decrypting: " + errorMsg);
            } else {
                System.out.println("!!ERROR: Unexpected response from Encryption Program.");
                log("DECRYPT", "Unexpected response for DECRYPT: " + response);
            }

            promptContinue();

        } catch (NumberFormatException e) {
            System.out.println("!!ERROR: Invalid input. Please enter a number.");
            log("HISTORY", "Invalid history selection input: " + selection);
            promptContinue();
        } catch (IOException e) {
            System.out.println("!!ERROR: Communication error with Encryption Program: " + e.getMessage());
            log("DECRYPT", "Communication error during DECRYPT: " + e.getMessage());
        }
    }

    // quiting the  program
    private static void handleQuit() {
        try {
           
            encryptionWriter.write("QUIT");
            encryptionWriter.newLine();
            encryptionWriter.flush();

            // Send QUIT command to Logger
            loggerWriter.write("QUIT");
            loggerWriter.newLine();
            loggerWriter.flush();

           
            log("STOPPED", "Logging Stopped.");
            encryptionProcess.waitFor();
            loggerProcess.waitFor();

            // Closing the streams
            encryptionWriter.close();
            encryptionReader.close();
            loggerWriter.close();

            System.out.println("Program terminated successfully.");

        } catch (IOException | InterruptedException e) {
            System.out.println("!!ERROR: Error during termination: " + e.getMessage());
            log("TERMINATION_ERROR", "Termination error: " + e.getMessage());
        }
    }

    // the continue button
    private static void promptContinue() {
        System.out.println("\n");
        System.out.print("Hit Enter to continue.");
        scanner.nextLine();
    }
}
