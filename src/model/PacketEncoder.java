package model;

import control.Header;
import view.DebugInterface;

import java.util.Arrays;

/**
 * Encodes a packet which needs to be sent.
 */
public class PacketEncoder {
    private byte[] data;
    private byte[][] fragmented;
    private int dataLength;
    private Header standardHeader;

    public byte[][] fragmentedMessage() {
        int numPackets = numPackets(dataLength);
        if (numPackets > 1) standardHeader.setFrag(true);

        fragmented = new byte[numPackets][];

        int packetDataLength = 29;
        byte[] packet;
        byte[] packetData;
        for (int fragNum = 0; fragNum < numPackets; fragNum++) {
            if ((fragNum + 1)  == numPackets) {
                packetDataLength = (dataLength - (29 * fragNum)) % 29;
            }

            packetData = Arrays.copyOfRange(data, fragNum * 29, fragNum * 29 + packetDataLength);
            packet = new byte[32];

            standardHeader.setDataLen(packetDataLength);
            standardHeader.setFragNum(fragNum);

            // copy the header into the packet array
            System.arraycopy(standardHeader.toByteArray(), 0, packet, 0, Header.HEADER_LENGTH);
            // copy the data into the packet array
            System.arraycopy(packetData, 0, packet, Header.HEADER_LENGTH, packetDataLength);

//            if ((fragNum + 1)  == numPackets) {
//                packet[Header.HEADER_LENGTH + packetDataLength] = 0x03;
//            }

            fragmented[fragNum] = packet;
        }

        return fragmented;
    }

    public PacketEncoder(byte[] data, Header standardHeader) {
        this.data = data;
        dataLength = data.length;
        if (data.length > 29) {
            byte[] dataFrag = new byte[data.length + 1];
            System.arraycopy(data, 0, dataFrag, 0, data.length);
            dataFrag[dataFrag.length-1] = 0x03;
            dataLength++;
            this.data = dataFrag;
        }
        this.standardHeader = standardHeader;
    }

    private int numPackets(int n) {
        int extra = 1;
        int length = Header.HEADER_LENGTH;
        if (n % 29 == 0) {
            extra = 0;
        }
        return (n - n % 29) / 29 + extra;
    }

    public byte[] createPacket(Header header, byte[] data) {
        byte[] packet = new byte[32];
        // copy the header into the packet array
        System.arraycopy(header.toByteArray(), 0, packet, 0, Header.HEADER_LENGTH);
        // copy the data into the packet array
        System.arraycopy(data, 0, packet, Header.HEADER_LENGTH, data.length);
        return packet;
    }

    public void run() {

    }
}
