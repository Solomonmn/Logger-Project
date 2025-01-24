import java.io.*;
import java.util.Scanner;

public class encrypt {
    private static String passkey = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) {
                if (!scanner.hasNextLine()) {
                    break; 
                }
                String inputLine = scanner.nextLine();
                if (inputLine.trim().isEmpty()) {
                    continue; 
                }
                String[] parts = inputLine.split(" ", 2);
                String command = parts[0].toUpperCase();

                switch (command) {
                    case "PASSKEY":
                        handlePasskey(parts);
                        break;

                    case "ENCRYPT":
                        handleEncrypt(parts);
                        break;

                    case "DECRYPT":
                        handleDecrypt(parts);
                        break;

                    case "QUIT":
                        scanner.close();
                        return;

                    default:
                        System.out.println("!!ERROR: Unknown command.");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("!!ERROR: Exception in encrypt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handlePasskey(String[] parts) {
        if (parts.length < 2) {
            System.out.println("!!ERROR: Missing passkey.");
            return;
        }
        passkey = parts[1].toUpperCase(); 
        System.out.println("RESULT");
    }

    private static void handleEncrypt(String[] parts) {
        if (passkey == null) {
            System.out.println("!!ERROR: Passkey not set.");
            return;
        }
        if (parts.length < 2) {
            System.out.println("!!ERROR: Missing text to encrypt.");
            return;
        }
        String plaintext = parts[1];
        String ciphertext = vigenereCipher(plaintext, passkey, true).toUpperCase(); 
        System.out.println("RESULT " + ciphertext);
    }

    private static void handleDecrypt(String[] parts) {
        if (passkey == null) {
            System.out.println("!!ERROR: Passkey not set.");
            return;
        }
        if (parts.length < 2) {
            System.out.println("!!ERROR: Missing text to decrypt.");
            return;
        }
        String ciphertext = parts[1];
        String decryptedText = vigenereCipher(ciphertext, passkey, false).toUpperCase(); 
        System.out.println("RESULT " + decryptedText);
    }

    // Vigenère Cipher Implementation
    private static String vigenereCipher(String text, String key, boolean encrypt) {
        StringBuilder result = new StringBuilder();
        key = key.toUpperCase();
        int keyLen = key.length();
        int keyIndex = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int keyShift = key.charAt(keyIndex % keyLen) - 'A';
                if (!encrypt) {
                    keyShift = 26 - keyShift;
                }
                char transformedChar = (char) ((Character.toUpperCase(ch) - 'A' + keyShift) % 26 + 'A');
                result.append(transformedChar);
                keyIndex++;
            } else {
                result.append(ch); 
            }
        }

        return result.toString();
    }
}
