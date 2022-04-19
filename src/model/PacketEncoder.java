package model;

import control.Header;

import java.util.Arrays;

/**
 * Encodes a packet which needs to be sent.
 */
public class PacketEncoder {
    private byte[] data;
    private byte[][] fragmented;
    private int dataLength;
    private Header standardHeader;

    /**
     * Returns the message as a fragmented byte arrays.
     *
     * @return message as a fragmented byte arrays
     */
    public byte[][] fragmentedMessage() {
        int numPackets = numPackets(dataLength);
        if (numPackets > 1) {
            standardHeader.setFrag(true);
        }

        fragmented = new byte[numPackets][];

        int packetDataLength = 29;
        byte[] packet;
        byte[] packetData;
        for (int fragNum = 0; fragNum < numPackets; fragNum++) {
            if ((fragNum + 1) == numPackets) {
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

            fragmented[fragNum] = packet;
        }

        return fragmented;
    }

    /**
     * Constructor for the packet encoder.
     *
     * @param data           data which needs to be encoded
     * @param standardHeader a standard header for all fragments
     */
    public PacketEncoder(byte[] data, Header standardHeader) {
        this.data = data;
        dataLength = data.length;
        if (data.length > 29) {
            byte[] dataFrag = new byte[data.length + 1];
            System.arraycopy(data, 0, dataFrag, 0, data.length);
            dataFrag[dataFrag.length - 1] = 0x03;
            dataLength++;
            this.data = dataFrag;
        }
        this.standardHeader = standardHeader;
    }

    /**
     * Returns how many fragments should be made for the given data.
     *
     * @param n the data length
     * @return how many fragments are needed for a certain amount of data
     */
    private int numPackets(int n) {
        int extra = 1;
        if (n % 29 == 0) {
            extra = 0;
        }
        return (n - n % 29) / 29 + extra;
    }
}
