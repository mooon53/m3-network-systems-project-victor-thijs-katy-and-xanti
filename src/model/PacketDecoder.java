package model;

import control.*;
import view.UI;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static utils.HelpFunc.*;
import static control.Client.*;

/**
 * Decodes a packet, converting all the bytes into the needed information.
 */
public class PacketDecoder implements Runnable {
    byte[] packet;
    int source;
    int dest;
    boolean syn;
    boolean ack;
    boolean frag;
    boolean dm;
    int seqNum;
    int dataLen;
    int nxtHop;
    int fragNum;
    String message;

    /**
     * Constructor for the packet decoder.
     *
     * @param packet byte array which needs to be decoded
     */
    public PacketDecoder(byte[] packet) {
        this.packet = packet;
    }

    /**
     * Runs when called decoding a packet.
     */
    public void run() {
        System.out.print(byteToString(packet[0]));
        System.out.print(byteToString(packet[1]));
        System.out.println(byteToString(packet[2]));
        String firstByte = byteToString(packet[0]);
        source = Integer.valueOf(firstByte.substring(0, 2), 2);
        dest = Integer.valueOf(firstByte.substring(2, 4), 2);
        syn = Integer.parseInt(firstByte.substring(4, 5)) == 1;
        ack = Integer.parseInt(firstByte.substring(5, 6)) == 1;
        frag = Integer.parseInt(firstByte.substring(6, 7)) == 1;
        dm = Integer.parseInt(firstByte.substring(7, 8)) == 1;
        String secondByte = byteToString(packet[1]);
        seqNum = Integer.valueOf(secondByte.substring(0, 5), 2);
        String dataLenPart = secondByte.substring(5, 8);
        System.out.println("datalenpart " + dataLenPart);
        String thirdByte = byteToString(packet[2]);
        dataLen = Integer.valueOf(dataLenPart + thirdByte.substring(0, 2), 2);
        System.out.println("datalen bits " + thirdByte.substring(0, 2) + dataLenPart);
        nxtHop = Integer.valueOf(thirdByte.substring(2, 4), 2);
        fragNum = Integer.valueOf(thirdByte.substring(4, 8), 2);
        byte[] messageBytes = new byte[dataLen];
        System.out.println("datalength " + dataLen);
        System.arraycopy(packet, 3, messageBytes, 0, dataLen);
        message = new String(messageBytes, StandardCharsets.UTF_8);
        handlePackage();
    }

    /**
     * Handles a packet according to the information in the header.
     */
    private void handlePackage() {
        //System.out.println("test");
        FragHandler fragHandler = new FragHandler();
        if (fragHandlerExists(seqNum)) {
            fragHandler = getFragHandler(seqNum);
        }
        // TODO: make this check if we've received the message before
        if (!fragHandler.hasFragment(fragNum)) {
            Fragment fragment = new Fragment(source, seqNum, fragNum, message);
            if (frag && !fragHandlerExists(seqNum)) {
                fragHandler = new FragHandler(fragment);
                Thread fragHandlerThread = new Thread(fragHandler);
                fragHandlerThread.start();
                addFragHandler(seqNum, fragHandler);
            } else if (frag) {
                getFragHandler(seqNum).addFragment(fragment);
            } else {
                Message fullMessage = new Message(source, message);
                UI.printMessage(fullMessage);
            }
        }
        // TODO: add a condition to check if all reachable nodes have received this packet
        if (true) {
            sendPacket();
        }
    }

    /**
     * Forwards a packet which needs to be forwarded to MyProtocol.
     *
     * @param ack whether the packet is an acknowledgement
     */
    private void sendPacket(boolean ack) {
        if (ack) {
            packet[0] = setBit(packet[0], 5);
        }
        // make a new byte buffer with the length of the input string
        ByteBuffer toSend = ByteBuffer.allocate(dataLen + 3);
        toSend.put(packet, 0, dataLen + 3); // copy the input string into the byte buffer.
        Packet msg;
        msg = new Packet(PacketType.DATA, toSend);
        MyProtocol.sendMessage(msg);
    }

    /**
     * Forwards a packet which needs to be forwarded to MyProtocol
     * with its acknowledgment set to true as a standard.
     */
    private void sendPacket() {
        sendPacket(true);
    }
}
