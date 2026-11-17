package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MainTest {

    @BeforeEach
    void setUp() {
        Main.resetMessages();
    }

    @Test
    void messageHashIsCorrectForTestCase1() {
        assertEquals("00:0:HITONIGHT",
                Main.createMessageHash("0012345678", 0, "Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    void remainingMessageHashesTestedInLoop() {
        String[] ids = {"0012345678", "0198765432"};
        String[] messages = {
            "Hi Mike, can you join us for dinner tonight?",
            "Thanks for the help"
        };
        String[] expected = {"00:0:HITONIGHT", "01:1:THANKSHELP"};

        for (int i = 0; i < messages.length; i++) {
            assertEquals(expected[i], Main.createMessageHash(ids[i], i, messages[i]));
        }
    }

    @Test
    void messageIDIsCreatedAndIsTenDigits() {
        String id = Main.generateMessageID();
        assertEquals(10, id.length());
        assertTrue(id.matches("[0-9]{10}"));
        assertTrue(Main.checkMessageID(id));
        System.out.println("Message ID generated: " + id);
    }

    @Test
    void messageIDLongerThanTenFails() {
        assertFalse(Main.checkMessageID("12345678901"));
    }

    @Test
    void recipientNumberCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.",
                Main.checkRecipientCell("+27718693002"));
    }

    @Test
    void recipientNumberIncorrectlyFormatted() {
        String expected = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        assertEquals(expected, Main.checkRecipientCell("08575975889"));
    }

    @Test
    void messageUnder250CharactersIsAccepted() {
        assertEquals("Message sent",
                Main.checkMessageLength("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    void messageOver250CharactersIsRejected() {
        String longMessage = "a".repeat(251);
        assertEquals("Please enter a message of less than 250 characters.",
                Main.checkMessageLength(longMessage));
    }

    @Test
    void sentMessageOptions() {
        assertEquals("Message successfully sent.", Main.sentMessage(1));
        assertEquals("Press 0 to delete the message.", Main.sentMessage(2));
        assertEquals("Message successfully stored.", Main.sentMessage(3));
    }

    @Test
    void totalMessagesSentIsReturned() {
        Main.addSentMessage("0012345678", "00:0:HITONIGHT", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Main.addSentMessage("0198765432", "01:1:THANKSHELP", "+27718693002",
                "Thanks for the help");
        assertEquals(2, Main.returnTotalMessages());
    }

    @Test
    void printMessagesContainsAllDetailsInOrder() {
        Main.addSentMessage("0012345678", "00:0:HITONIGHT", "+27718693002", "Hi tonight");
        String output = Main.printMessages();
        int id = output.indexOf("Message ID");
        int hash = output.indexOf("Message Hash");
        int recipient = output.indexOf("Recipient");
        int message = output.indexOf("Message: ");
        assertTrue(id < hash && hash < recipient && recipient < message);
    }

    // ===================== PART 3 TESTS =====================

    @Test
    void arraysArePopulatedWithTestData() {
        Main.populateTestData();
        assertEquals(2, Main.sentMessageTexts.size());
        assertEquals(2, Main.storedMessageTexts.size());
        assertEquals(1, Main.disregardedMessages.size());
        // the parallel arrays must all be the same length
        assertEquals(Main.sentMessageTexts.size(), Main.sentMessageIDs.size());
        assertEquals(Main.sentMessageTexts.size(), Main.sentMessageHashes.size());
        assertEquals(Main.sentMessageTexts.size(), Main.sentRecipients.size());
        assertEquals("Did you get the cake?", Main.sentMessageTexts.get(0));
        assertEquals("It is dinner time!", Main.sentMessageTexts.get(1));
        assertEquals("Yohoo, I am at your gate.", Main.disregardedMessages.get(0));
    }

    @Test
    void longestSentMessageIsDisplayed() {
        Main.populateTestData();
        String output = Main.displayLongestMessage();
        assertTrue(output.contains("Did you get the cake?"));
        assertTrue(output.contains("+27834557896"));
        assertFalse(output.contains("It is dinner time!"));
    }

    @Test
    void longestMessageWithNoSentMessages() {
        assertEquals("There are no sent messages.", Main.displayLongestMessage());
    }

    @Test
    void searchByRecipientFindsStoredMessages() {
        Main.populateTestData();
        ArrayList<String> found = Main.searchMessagesByRecipient("+27838884567");
        assertEquals(2, found.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.", found.get(0));
        assertEquals("Ok, I am leaving without you.", found.get(1));
    }

    @Test
    void searchByRecipientFindsSentMessages() {
        Main.populateTestData();
        ArrayList<String> found = Main.searchMessagesByRecipient("+27834557896");
        assertEquals(1, found.size());
        assertEquals("Did you get the cake?", found.get(0));
    }

    @Test
    void searchByUnknownRecipientFindsNothing() {
        Main.populateTestData();
        assertTrue(Main.searchMessagesByRecipient("+27000000000").isEmpty());
    }

    @Test
    void deleteSentMessageUsingHash() {
        Main.populateTestData();
        String hash = Main.sentMessageHashes.get(1);
        assertEquals("Message \"It is dinner time!\" successfully deleted.", Main.deleteMessageByHash(hash));
        assertEquals(1, Main.sentMessageTexts.size());
        assertEquals(1, Main.sentMessageIDs.size());
        assertEquals(1, Main.sentMessageHashes.size());
        assertEquals(1, Main.sentRecipients.size());
        assertEquals(1, Main.returnTotalMessages());
    }

    @Test
    void deleteStoredMessageUsingHash() {
        Main.populateTestData();
        String hash = Main.storedMessageHashes.get(0);
        String expected = "Message \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.";
        assertEquals(expected, Main.deleteMessageByHash(hash));
        assertEquals(1, Main.storedMessageTexts.size());
        assertEquals("Ok, I am leaving without you.", Main.storedMessageTexts.get(0));
    }

    @Test
    void deleteWithUnknownHashRemovesNothing() {
        Main.populateTestData();
        assertEquals("No message with the hash 99:9:NOPE was found.", Main.deleteMessageByHash("99:9:NOPE"));
        assertEquals(2, Main.sentMessageTexts.size());
        assertEquals(2, Main.storedMessageTexts.size());
    }

    @Test
    void messageReportContainsAllSentMessages() {
        Main.populateTestData();
        String report = Main.displayMessageReport();
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("It is dinner time!"));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains(Main.sentMessageHashes.get(0)));
        assertTrue(report.contains(Main.sentMessageIDs.get(1)));
        // stored and disregarded messages are not part of the sent report
        assertFalse(report.contains("Yohoo"));
        assertFalse(report.contains("Ok, I am leaving without you."));
    }

    @Test
    void messageReportWithNoSentMessages() {
        assertEquals("There are no sent messages.", Main.displayMessageReport());
    }

    @Test
    void jsonFileIsReadIntoStoredArrays(@TempDir Path folder) throws IOException {
        Path file = folder.resolve("messages.json");
        Files.writeString(file, "[\n"
                + "  {\n"
                + "    \"messageID\": \"0012345678\",\n"
                + "    \"messageHash\": \"00:0:DIDCAKE\",\n"
                + "    \"recipient\": \"+27838884567\",\n"
                + "    \"message\": \"She said \\\"hi\\\" {ok}\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"messageID\": \"0198765432\",\n"
                + "    \"messageHash\": \"01:1:OKYOU\",\n"
                + "    \"recipient\": \"+27834484567\",\n"
                + "    \"message\": \"Ok, I am leaving without you.\"\n"
                + "  }\n"
                + "]\n");

        assertEquals(2, Main.readJsonFile(file.toString()));
        assertEquals("0012345678", Main.storedMessageIDs.get(0));
        assertEquals("00:0:DIDCAKE", Main.storedMessageHashes.get(0));
        assertEquals("+27838884567", Main.storedRecipients.get(0));
        assertEquals("She said \"hi\" {ok}", Main.storedMessageTexts.get(0));
        assertEquals("Ok, I am leaving without you.", Main.storedMessageTexts.get(1));
        assertTrue(Main.displayStoredMessages().contains("01:1:OKYOU"));
    }

    @Test
    void missingJsonFileReturnsMinusOne(@TempDir Path folder) {
        assertEquals(-1, Main.readJsonFile(folder.resolve("missing.json").toString()));
    }

    @Test
    void storedMessagesSurviveWriteAndRead() throws IOException {
        Main.storeMessage("0012345678", "00:0:HELLOWORLD", "+27718693002", "Hello \"world\"");
        Main.storeMessage("0198765432", "01:1:BYEBYE", "+27718693002", "Bye bye");
        Main.resetMessages();
        assertEquals(2, Main.readJsonFile("messages.json"));
        assertEquals("Hello \"world\"", Main.storedMessageTexts.get(0));
        assertEquals("01:1:BYEBYE", Main.storedMessageHashes.get(1));
        Files.deleteIfExists(Path.of("messages.json"));
    }
}
