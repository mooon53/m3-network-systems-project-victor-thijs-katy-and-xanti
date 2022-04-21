package model.objects;

import static utils.HelpFunc.*;

public class Ping {
    int sourceID;

    /**
     * Constructor for the ping object.
     *
     * @param source node ID from the source of the PING
     */
    public Ping(int source) {
        this.sourceID = source;
    }

    /**
     * Returns the data from a ping in a byte array.
     *
     * @return byte array of the ping
     */
    public byte[] toBytes() {
        String source = padString(Integer.toBinaryString(sourceID), 2);
        String firstByte = source + "000000";
        String secondByte = "00000000";
        byte[] output = new byte[2];
        output[0] = stringToByte(firstByte);
        output[1] = stringToByte(secondByte);
        return output;
    }

    /**
     * Gets the source's node ID corresponding to the ping.
     *
     * @return source's node ID corresponding to the ping
     */
    public int getSourceID() {
        return sourceID;
    }
}
