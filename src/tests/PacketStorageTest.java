package tests;

import model.objects.Client;
import model.objects.PacketStorage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods from PacketStorage.
 */
public class PacketStorageTest {
    @Test
    void testId() {
        PacketStorage packetStorage = new PacketStorage(new Client("netsys.ewi.utwente.nl", 8954, 500 + 9 * 100,
                new LinkedBlockingQueue<>(), new LinkedBlockingQueue<>(), 0));
        for (int seqNum = 0; seqNum < 16; seqNum++) {
            for (int fragNum = 0; fragNum < 16; fragNum++) {
                assertEquals(seqNum * 32 + fragNum, packetStorage.id(seqNum, fragNum));
            }
        }
    }
}
