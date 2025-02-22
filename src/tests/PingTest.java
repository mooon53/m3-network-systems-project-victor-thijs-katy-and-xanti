package tests;

import model.objects.Ping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the ping object.
 */
public class PingTest {
    @Test
    void testToBytes() {
        Ping ping = new Ping(3);
        assertEquals(64 - 128, ping.toBytes()[0]);
        assertEquals(0, ping.toBytes()[1]);
    }
}
