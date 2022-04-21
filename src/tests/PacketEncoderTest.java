package tests;

import model.objects.Header;
import model.PacketEncoder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests PacketEncoder.
 */
public class PacketEncoderTest {
    @Test
    void testFragmentedMessage() {
        String input = "012345678901234567890123456789";
        byte[] inputBytes = input.getBytes(); // get bytes from input
        Header standardHeader = new Header(0, 0, false, false, false, false, 0,
                inputBytes.length, 0, 0);
        PacketEncoder packetEncoder = new PacketEncoder(inputBytes, standardHeader);
        byte[][] fragmentedMessage = packetEncoder.fragmentedMessage();

        assertEquals(2, fragmentedMessage.length);

        //check if the data-length bits are correct
        assertEquals(7, fragmentedMessage[0][1]);
        assertEquals(0, fragmentedMessage[1][1]);
    }

}
