package com.mycompany.main;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    // ===================== PART 1 : REGISTRATION & LOGIN =====================

    static Scanner input = new Scanner(System.in);

    static String username;
    static String password;
    static String cellphone;

    static String registeredUsername;
    static String registeredPassword;
    static String registeredCellphone;

    public static boolean checkUserName(String username) {
        return username.length() == 5 && username.contains("_");
    }

    public static boolean checkPasswordComplexity(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    public static boolean checkCellphone(String number) {
        return number.matches("^\\+27[0-9]{9}$");
    }

    public static void registerUser() {
        System.out.print("Enter your username: ");
        username = input.nextLine();

        System.out.print("Enter your password: ");
        password = input.nextLine();

        System.out.print("Enter your cellphone number (+27): ");
        cellphone = input.nextLine();

        boolean usernameCorrect = checkUserName(username);
        boolean passwordCorrect = checkPasswordComplexity(password);
        boolean phoneCorrect = checkCellphone(cellphone);

        if (usernameCorrect && passwordCorrect && phoneCorrect) {
            registeredUsername = username;
            registeredPassword = password;
            registeredCellphone = cellphone;
            System.out.println("User registered successfully.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    public static boolean loginUser(String username, String password) {
        return username.equals(registeredUsername)
                && password.equals(registeredPassword);
    }

    public static String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome, it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }

    // ===================== PART 2 : SENDING MESSAGES =====================

    // Each sent / stored message is kept as {messageID, messageHash, recipient, message}
    static ArrayList<String[]> sentMessages = new ArrayList<>();
    static ArrayList<String[]> storedMessages = new ArrayList<>();

    static final Random random = new Random();

    // ===================== PART 3 : PARALLEL ARRAYS =====================
    // Index n of every list in a group refers to the same message.
    // Sent messages: ID, hash, recipient and message text
    static ArrayList<String> sentMessageIDs = new ArrayList<>();
    static ArrayList<String> sentMessageHashes = new ArrayList<>();
    static ArrayList<String> sentRecipients = new ArrayList<>();
    static ArrayList<String> sentMessageTexts = new ArrayList<>();

    // Stored messages: ID, hash, recipient and message text
    static ArrayList<String> storedMessageIDs = new ArrayList<>();
    static ArrayList<String> storedMessageHashes = new ArrayList<>();
    static ArrayList<String> storedRecipients = new ArrayList<>();
    static ArrayList<String> storedMessageTexts = new ArrayList<>();

    // Disregarded messages (message text only)
    static ArrayList<String> disregardedMessages = new ArrayList<>();

    // Creates a random ten-digit Message ID (leading zeros are kept, e.g. 0012345678)
    public static String generateMessageID() {
        long number = (long) (random.nextDouble() * 10_000_000_000L);
        return String.format("%010d", number);
    }

    // Message ID must be no more than ten characters
    public static boolean checkMessageID(String messageID) {
        return messageID != null && messageID.length() <= 10;
    }

    // Reuses the Part 1 cellphone check
    public static String checkRecipientCell(String recipient) {
        if (checkCellphone(recipient)) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    // Message may not exceed 250 characters
    public static String checkMessageLength(String message) {
        if (message.length() > 250) {
            return "Please enter a message of less than 250 characters.";
        } else {
            return "Message sent";
        }
    }

    // Hash = first two digits of ID : message number : FIRSTWORDLASTWORD (all caps)
    public static String createMessageHash(String messageID, int messageNumber, String message) {
        String firstTwo = messageID.substring(0, 2);

        String[] words = message.trim().split("\\s+");
        // Remove punctuation so "tonight?" becomes "tonight"
        String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");

        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Returns the feedback text for the user's send / disregard / store choice
    public static String sentMessage(int choice) {
        switch (choice) {
            case 1:
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    public static void addSentMessage(String id, String hash, String recipient, String message) {
        sentMessages.add(new String[]{id, hash, recipient, message});

        // Part 3: keep the parallel arrays in step
        sentMessageIDs.add(id);
        sentMessageHashes.add(hash);
        sentRecipients.add(recipient);
        sentMessageTexts.add(message);
    }

    // Returns all messages sent while the program is running
    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (String[] m : sentMessages) {
            sb.append("Message ID: ").append(m[0]).append("\n");
            sb.append("Message Hash: ").append(m[1]).append("\n");
            sb.append("Recipient: ").append(m[2]).append("\n");
            sb.append("Message: ").append(m[3]).append("\n\n");
        }
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    public static void resetMessages() {
        sentMessages.clear();
        storedMessages.clear();

        // Part 3 arrays
        sentMessageIDs.clear();
        sentMessageHashes.clear();
        sentRecipients.clear();
        sentMessageTexts.clear();
        storedMessageIDs.clear();
        storedMessageHashes.clear();
        storedRecipients.clear();
        storedMessageTexts.clear();
        disregardedMessages.clear();
    }

    // Escapes quotes and backslashes so the JSON stays valid
    private static String jsonEscape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Stores the message and rewrites messages.json with every stored message
    public static void storeMessage(String id, String hash, String recipient, String message) {
        storedMessages.add(new String[]{id, hash, recipient, message});

        // Part 3: keep the parallel arrays in step
        storedMessageIDs.add(id);
        storedMessageHashes.add(hash);
        storedRecipients.add(recipient);
        storedMessageTexts.add(message);

        writeStoredMessagesToJson();
    }

    // Rewrites messages.json with every stored message
    // (this is the original JSON-writing code from storeMessage, moved into its own method
    // so that deleting a stored message can reuse it)
    private static void writeStoredMessagesToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            String[] m = storedMessages.get(i);
            json.append("  {\n");
            json.append("    \"messageID\": \"").append(jsonEscape(m[0])).append("\",\n");
            json.append("    \"messageHash\": \"").append(jsonEscape(m[1])).append("\",\n");
            json.append("    \"recipient\": \"").append(jsonEscape(m[2])).append("\",\n");
            json.append("    \"message\": \"").append(jsonEscape(m[3])).append("\"\n");
            json.append("  }");
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");

        try (FileWriter writer = new FileWriter("messages.json")) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write messages.json: " + e.getMessage());
        }
    }

    // Reads a whole number, asking again until the input is valid
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Handles entering every message (the FOR loop)
    private static void sendMessages(int numMessages) {
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\n--- Message " + (i + 1) + " of " + numMessages + " ---");

            // Recipient
            String recipient;
            while (true) {
                System.out.print("Enter recipient cell number (e.g. +27718693002): ");
                recipient = input.nextLine().trim();
                String result = checkRecipientCell(recipient);
                System.out.println(result);
                if (checkCellphone(recipient)) {
                    break;
                }
            }

            // Message text
            String message;
            while (true) {
                System.out.print("Enter your message (max 250 characters): ");
                message = input.nextLine();
                String result = checkMessageLength(message);
                System.out.println(result);
                if (message.length() <= 250 && !message.isBlank()) {
                    break;
                }
            }

            // ID and hash (message number comes from the loop counter)
            String messageID = generateMessageID();
            if (!checkMessageID(messageID)) {
                System.out.println("Message ID is invalid.");
            }
            String messageHash = createMessageHash(messageID, i, message);
            System.out.println("Message ID generated: " + messageID);

            // Send / disregard / store
            System.out.println("Choose an option:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt("Option: ");
            while (choice < 1 || choice > 3) {
                choice = readInt("Please choose 1, 2 or 3: ");
            }
            System.out.println(sentMessage(choice));

            if (choice == 1) {
                addSentMessage(messageID, messageHash, recipient, message);
                System.out.println("\nMessage ID: " + messageID);
                System.out.println("Message Hash: " + messageHash);
                System.out.println("Recipient: " + recipient);
                System.out.println("Message: " + message);
            } else if (choice == 2) {
                String press = "";
                while (!press.equals("0")) {
                    System.out.print("Press 0 to delete: ");
                    press = input.nextLine().trim();
                }
                disregardedMessages.add(message);
                System.out.println("Message deleted.");
            } else {
                storeMessage(messageID, messageHash, recipient, message);
            }
        }

        System.out.println("\nTotal messages sent: " + returnTotalMessages());
        if (returnTotalMessages() > 0) {
            System.out.println("\nSent messages:\n" + printMessages());
        }
    }

    // ===================== PART 3 : ARRAY FEATURES =====================

    // Formats one message's details for display
    private static String formatMessage(String id, String hash, String recipient, String message) {
        return "Message ID: " + id + "\n"
                + "Message Hash: " + hash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + message + "\n";
    }

    // Adds a message that the user disregarded
    public static void addDisregardedMessage(String message) {
        disregardedMessages.add(message);
    }

    // Fills the arrays with the Part 3 test data. Recipients are added exactly as supplied
    // (message 4 is deliberately not in +27 format), so the cellphone check is not applied here.
    public static void populateTestData() {
        resetMessages();

        String[] recipients = {"+27834557896", "+27838884567", "+27834484567", "0838884567", "+27838884567"};
        String[] messages = {
            "Did you get the cake?",
            "Where are you? You are late! I have asked you to be on time.",
            "Yohoo, I am at your gate.",
            "It is dinner time!",
            "Ok, I am leaving without you."
        };
        // 1 = sent, 2 = disregarded, 3 = stored
        int[] flags = {1, 3, 2, 1, 3};

        for (int i = 0; i < messages.length; i++) {
            String id = generateMessageID();
            String hash = createMessageHash(id, i, messages[i]);

            if (flags[i] == 1) {
                addSentMessage(id, hash, recipients[i], messages[i]);
            } else if (flags[i] == 3) {
                storeMessage(id, hash, recipients[i], messages[i]);
            } else {
                addDisregardedMessage(messages[i]);
            }
        }
    }

    // Searches the sent message array for the longest message and returns its details
    public static String displayLongestMessage() {
        if (sentMessageTexts.isEmpty()) {
            return "There are no sent messages.";
        }

        int longestIndex = 0;
        for (int i = 1; i < sentMessageTexts.size(); i++) {
            if (sentMessageTexts.get(i).length() > sentMessageTexts.get(longestIndex).length()) {
                longestIndex = i;
            }
        }

        return formatMessage(sentMessageIDs.get(longestIndex), sentMessageHashes.get(longestIndex),
                sentRecipients.get(longestIndex), sentMessageTexts.get(longestIndex));
    }

    // Searches the sent and stored arrays and returns every message for the recipient
    public static ArrayList<String> searchMessagesByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();

        for (int i = 0; i < sentRecipients.size(); i++) {
            if (sentRecipients.get(i).equals(recipient)) {
                found.add(sentMessageTexts.get(i));
            }
        }
        for (int i = 0; i < storedRecipients.size(); i++) {
            if (storedRecipients.get(i).equals(recipient)) {
                found.add(storedMessageTexts.get(i));
            }
        }
        return found;
    }

    // Searches for the hash and removes the message from all of its arrays
    public static String deleteMessageByHash(String hash) {
        int sentIndex = sentMessageHashes.indexOf(hash);
        if (sentIndex >= 0) {
            String text = sentMessageTexts.get(sentIndex);
            sentMessageIDs.remove(sentIndex);
            sentMessageHashes.remove(sentIndex);
            sentRecipients.remove(sentIndex);
            sentMessageTexts.remove(sentIndex);
            sentMessages.remove(sentIndex);
            return "Message \"" + text + "\" successfully deleted.";
        }

        int storedIndex = storedMessageHashes.indexOf(hash);
        if (storedIndex >= 0) {
            String text = storedMessageTexts.get(storedIndex);
            storedMessageIDs.remove(storedIndex);
            storedMessageHashes.remove(storedIndex);
            storedRecipients.remove(storedIndex);
            storedMessageTexts.remove(storedIndex);
            storedMessages.remove(storedIndex);
            writeStoredMessagesToJson();
            return "Message \"" + text + "\" successfully deleted.";
        }

        return "No message with the hash " + hash + " was found.";
    }

    // Report of every sent message
    public static String displayMessageReport() {
        if (sentMessageTexts.isEmpty()) {
            return "There are no sent messages.";
        }

        StringBuilder report = new StringBuilder("===== SENT MESSAGES REPORT =====\n");
        for (int i = 0; i < sentMessageTexts.size(); i++) {
            report.append("\n").append(formatMessage(sentMessageIDs.get(i), sentMessageHashes.get(i),
                    sentRecipients.get(i), sentMessageTexts.get(i)));
        }
        return report.toString();
    }

    // Turns an escaped JSON string value back into normal text (reverse of jsonEscape)
    private static String jsonUnescape(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;
                c = text.charAt(i);
            }
            result.append(c);
        }
        return result.toString();
    }

    // Reads the JSON file into the stored message arrays (replacing what is there) and
    // returns the number of messages read, or -1 if the file could not be read.
    // Attribution: uses java.nio.file.Files and java.util.regex from the Java SE API
    // (Oracle, Java Platform SE documentation). The parsing code was written for the
    // messages.json layout produced by storeMessage.
    public static int readJsonFile(String fileName) {
        String content;
        try {
            content = Files.readString(Path.of(fileName));
        } catch (IOException e) {
            return -1;
        }

        String string = "\"((?:[^\"\\\\]|\\\\.)*)\"";
        Pattern pattern = Pattern.compile(
                "\"messageID\"\\s*:\\s*" + string + "\\s*,\\s*"
                + "\"messageHash\"\\s*:\\s*" + string + "\\s*,\\s*"
                + "\"recipient\"\\s*:\\s*" + string + "\\s*,\\s*"
                + "\"message\"\\s*:\\s*" + string);
        Matcher matcher = pattern.matcher(content);

        storedMessages.clear();
        storedMessageIDs.clear();
        storedMessageHashes.clear();
        storedRecipients.clear();
        storedMessageTexts.clear();

        int count = 0;
        while (matcher.find()) {
            String id = jsonUnescape(matcher.group(1));
            String hash = jsonUnescape(matcher.group(2));
            String recipient = jsonUnescape(matcher.group(3));
            String message = jsonUnescape(matcher.group(4));

            storedMessages.add(new String[]{id, hash, recipient, message});
            storedMessageIDs.add(id);
            storedMessageHashes.add(hash);
            storedRecipients.add(recipient);
            storedMessageTexts.add(message);
            count++;
        }
        return count;
    }

    // Displays the stored message arrays
    public static String displayStoredMessages() {
        if (storedMessageTexts.isEmpty()) {
            return "There are no stored messages.";
        }

        StringBuilder output = new StringBuilder("===== STORED MESSAGES =====\n");
        for (int i = 0; i < storedMessageTexts.size(); i++) {
            output.append("\n").append(formatMessage(storedMessageIDs.get(i), storedMessageHashes.get(i),
                    storedRecipients.get(i), storedMessageTexts.get(i)));
        }
        return output.toString();
    }

    // ===================== MAIN =====================

    public static void main(String[] args) {
        // Part 1: register until successful, then log in
        while (registeredUsername == null) {
            registerUser();
        }

        System.out.print("Login - enter your username: ");
        String loginUsername = input.nextLine();
        System.out.print("Login - enter your password: ");
        String loginPassword = input.nextLine();

        boolean loginSuccessful = loginUser(loginUsername, loginPassword);
        System.out.println(returnLoginStatus(loginSuccessful));

        // Messages can only be sent after a successful login
        if (!loginSuccessful) {
            return;
        }

        System.out.println("Welcome to QuickChat.");

        int numMessages = readInt("How many messages do you wish to enter? ");
        while (numMessages < 1) {
            numMessages = readInt("Please enter at least 1: ");
        }
        boolean messagesEntered = false;

        // Part 2: menu loop (the WHILE loop) - runs until the user quits
        int option = 0;
        while (option != 3) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Load Part 3 test data");
            System.out.println("5) Display the longest sent message");
            System.out.println("6) Search messages by recipient");
            System.out.println("7) Delete a message using its hash");
            System.out.println("8) Read messages.json and display it");
            option = readInt("Choose an option: ");

            if (option == 1) {
                if (messagesEntered) {
                    System.out.println("You have already entered all " + numMessages + " messages.");
                } else {
                    sendMessages(numMessages);
                    messagesEntered = true;
                }
            } else if (option == 2) {
                System.out.println(displayMessageReport());
            } else if (option == 3) {
                System.out.println("Goodbye.");
            } else if (option == 4) {
                populateTestData();
                messagesEntered = true;
                System.out.println("Test data loaded into the arrays.");
            } else if (option == 5) {
                System.out.println(displayLongestMessage());
            } else if (option == 6) {
                System.out.print("Enter the recipient cell number: ");
                String recipient = input.nextLine().trim();
                ArrayList<String> found = searchMessagesByRecipient(recipient);
                if (found.isEmpty()) {
                    System.out.println("No messages found for " + recipient + ".");
                } else {
                    System.out.println("Messages for " + recipient + ":");
                    for (String text : found) {
                        System.out.println("- " + text);
                    }
                }
            } else if (option == 7) {
                System.out.print("Enter the message hash to delete: ");
                System.out.println(deleteMessageByHash(input.nextLine().trim().toUpperCase()));
            } else if (option == 8) {
                int count = readJsonFile("messages.json");
                if (count < 0) {
                    System.out.println("Could not read messages.json. Store a message first.");
                } else {
                    System.out.println(count + " message(s) read from messages.json.");
                    System.out.println(displayStoredMessages());
                }
            } else {
                System.out.println("Please choose 1 to 8.");
            }
        }
    }
}
