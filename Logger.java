import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Logger {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Logger <log_file>");
            System.exit(1);
        }

        String logFileName = args[0];

        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFileName, true));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                if (!scanner.hasNextLine()) {
                    break; // End of input
                }
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("QUIT")) {
                    // Log the STOPPED action before quitting
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                    String logEntry = String.format("%s [STOPPED] Logging Stopped.", timestamp);
                    logWriter.write(logEntry);
                    logWriter.newLine();
                    logWriter.flush();
                    break;
                }


                String[] parts = input.split(" ", 2);
                if (parts.length < 2) {
                    continue; 
                }

                String action = parts[0].toUpperCase();
                String message = parts[1];

                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                String logEntry = String.format("%s [%s] %s", timestamp, action, message);

                logWriter.write(logEntry);
                logWriter.newLine();
                logWriter.flush();
            }

        } catch (IOException e) {
            System.err.println("An error hs been encountered: " + e.getMessage());
            System.exit(1);
        }
    }
}
