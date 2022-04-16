package view;

import control.PacketType;
import model.PacketDecoder;

/**
 * Debugging interface printing statements.
 */
public class DebugInterface {
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * Formats the debugging print statements.
     * @param string text string which needs formatting
     * @return formatted string
     */
    private static String debuggingFormat(String string) {
        return ANSI_YELLOW + "[" + string + "]" + ANSI_RESET;
    }

    /**
     * Prints the type of packets in the right format.
     * @param packetType type of the packet.
     */
    public static void printPacketType(PacketType packetType) {
        System.out.println(debuggingFormat(packetType.toString().toUpperCase()));
    }

    /**
     * Prints the set-up of a node with its node ID.
     * @param node ID of the node
     */
    public static void printSetup(int node) {
        System.out.println(debuggingFormat("Your node ID is " + node));
    }

    /**
     * Prints the information contained in the header.
     * @param packetDecoder
     */
    public static void printHeaderInformation(PacketDecoder packetDecoder) {
        System.out.println(debuggingFormat(packetDecoder.getHeader().toString()));
    }
}
