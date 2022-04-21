package tests;

import model.*;
import model.objects.Client;
import model.objects.Packet;
import model.objects.PacketStorage;
import model.objects.PacketType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods from Header.
 */
public class PacketDecoderTest {
    private PacketDecoder packetDecoder;

    @Test
    void testPacketDecoder() {
        byte[] data = new byte[32];
        data[0] = 6;
        ByteBuffer toSend = ByteBuffer.allocate(data.length);
        toSend.put(data, 0, data.length);
        Packet packet = new Packet(PacketType.DATA, toSend);

        packetDecoder = new PacketDecoder(data, packet, new PacketStorage(
                new Client("netsys.ewi.utwente.nl", 8954, 500 + 9 * 100,
                        new LinkedBlockingQueue<>(), new LinkedBlockingQueue<>(), 0)));

        String string = "source = " + 0 + ", " + "destination = " + 0 + ", "
                + "syn-flag = " + false + ", " + "ack-flag = " + true + ", "
                + "frag-flag = " + true + ", " + "ack-flag = " + true + ", "
                + "direct message-flag = " + false + ", " + "sequence number = " + 0 + ", "
                + "data length = " + 0 + ", " + "next hop = " + 0 + ", "
                + "fragmentation number = " + 0;

        Thread thread = new Thread(packetDecoder);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(string, packetDecoder.getHeader().toString());
    }
}