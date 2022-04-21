package tests;

import control.Header;
import model.PacketDecoder;
import model.PacketEncoder;
import utils.HelpFunc;
import view.DebugInterface;
import view.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import control.Fragment;
import control.Header;
import model.FragHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.HelpFunc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests PacketEncoder.
 */
public class PacketEncoderTest {


    @Test
    void testNumPackets() {
        String string;
    }

    @Test
    void testFragmentedMessage() {
        int nodeID = 0;
        int sequenceNumber = 0;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;

        input = "";
        byte[] inputBytes = input.getBytes(); // get bytes from input
        Header standardHeader = new Header(nodeID, 0, false, false, false, false, sequenceNumber,
                0, nodeID, 0);
        PacketEncoder packetEncoder = new PacketEncoder(inputBytes, standardHeader);
        packetEncoder.fragmentedMessage();

    }

}
