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

public class PacketEncoderTest {

    public static void main(String[] args) throws IOException {
        int nodeID = 0;
        int sequenceNumber = 0;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;

        UI ui = new UI();
        input = ui.getInput(br);
        while (input.length() > 463) {
            input = ui.getInput(br, "Please put in a message " +
                    "with a maximum of 463 characters");
        }
        byte[] inputBytes = input.getBytes(); // get bytes from input
        Header standardHeader = new Header(nodeID, 0, false, false, false, false, sequenceNumber,
                0, nodeID, 0);
        PacketEncoder packetEncoder = new PacketEncoder(inputBytes, standardHeader);

        for (byte[] packet : packetEncoder.fragmentedMessage()) {
            PacketDecoder packetDecoder = new PacketDecoder(packet);
            packetDecoder.decode();
            DebugInterface.printHeaderInformation(packetDecoder);
        }
    }

}
