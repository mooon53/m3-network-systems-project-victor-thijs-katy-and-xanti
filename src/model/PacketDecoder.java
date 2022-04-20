package model;

import control.*;
import view.*;

import java.nio.charset.StandardCharsets;

import static control.MyProtocol.DEBUGGING_MODE;
import static utils.HelpFunc.*;
import static control.Client.*;

/**
 * Decodes a packet, converting all the bytes into the needed information.
 */
public class PacketDecoder implements Runnable {
    PacketStorage packetStorage;
    Packet packet;

    byte[] packetBytes;
    String message;
    Header header;

    // TODO: decide if we want to remove these
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

    /**
     * Constructor for the packet decoder.
     *
     * @param packet byte array which needs to be decoded
     */
    public PacketDecoder(byte[] packetBytes, Packet packet, PacketStorage packetStorage) {
        this.packetStorage = packetStorage;
        this.packet = packet;
        this.packetBytes = packetBytes;
    }

    /**
     * Runs when called decoding a packet.
     */
    public void run() {
        // separate every part of the header from the first byte
        String firstByte = byteToString(packetBytes[0]);
        source = Integer.valueOf(firstByte.substring(0, 2), 2);
        dest = Integer.valueOf(firstByte.substring(2, 4), 2);
        syn = Integer.parseInt(firstByte.substring(4, 5)) == 1;
        ack = Integer.parseInt(firstByte.substring(5, 6)) == 1;
        if (DEBUGGING_MODE) System.out.println("frag flag = " + Integer.parseInt(firstByte.substring(6, 7)));
        frag = Integer.parseInt(firstByte.substring(6, 7)) == 1;
        dm = Integer.parseInt(firstByte.substring(7, 8)) == 1;

        // separate every part of the header from the second byte
        String secondByte = byteToString(packetBytes[1]);
        seqNum = Integer.valueOf(secondByte.substring(0, 5), 2);
        String dataLenPart = secondByte.substring(5, 8);

        // separate every part of the header from the third byte
        String thirdByte = byteToString(packetBytes[2]);
        dataLen = Integer.valueOf(dataLenPart + thirdByte.substring(0, 2), 2);
        nxtHop = Integer.valueOf(thirdByte.substring(2, 4), 2);
        fragNum = Integer.valueOf(thirdByte.substring(4, 8), 2);

        // create a header with all the abstracted information
        header = new Header(source, dest, syn, ack, frag, dm, seqNum, dataLen, nxtHop, fragNum);
        // abstract the actual text data from the packet
        byte[] messageBytes = new byte[dataLen];
        System.arraycopy(packetBytes, 3, messageBytes, 0, dataLen);
        message = new String(messageBytes, StandardCharsets.UTF_8);
        handlePacket();
    }

    /**
     * Handles a packet according to the information in the header.
     */
    private void handlePacket() {
        if (DEBUGGING_MODE) {
            System.out.println(bytesToString(packetBytes));
            System.out.println("creating fragment seq " + header.getSeqNum() + " frag " + header.getFragNum() + " fragment flag set to " + header.getFrag());
        }
        Fragment fragment;
        fragment = new Fragment(header, message);
        packetStorage.addPacket(fragment, packet);
    }

    /**
     * Forwards a packet which needs to be forwarded to MyProtocol.
     *
     * @param ack whether the packet is an acknowledgement
     */
    private void sendPacket(boolean ack) {
        if (ack) {
            packetBytes[0] = setBit(packetBytes[0], 5);
        }
        //TODO: set all header bits correctly maybe
        MyProtocol.sendPacket(packetBytes);
    }

    /**
     * Forwards a packet which needs to be forwarded to MyProtocol
     * with its acknowledgment flag set to true as a standard.
     */
    private void sendPacket() {
        sendPacket(true);
    }

    /**
     * Gets the header object from the packetDecoder.
     *
     * @return header object from the packetDecoder
     */
    public Header getHeader() {
        return header;
    }
}
