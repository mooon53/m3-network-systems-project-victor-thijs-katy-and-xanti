package control;

import static utils.HelpFunc.padString;
import static utils.HelpFunc.stringToByte;

/**
 * Header object containing information and can be exported to byte array.
 */
public class Header {
    private int source;
    private int dest;
    private boolean syn;
    private boolean ack;
    private boolean frag;
    private boolean dm;
    private int seqNum;
    private int dataLen;
    private int nxtHop;
    private int fragNum;

    static final int HEADER_LENGTH = 3;

    /**
     * Constructor for the header object.
     *
     * @param source  source node ID
     * @param dest    destination node ID
     * @param syn     synchronization flag
     * @param ack     acknowledgement flag
     * @param frag    fragmentation flag
     * @param dm      direct-message flag
     * @param seqNum  sequence number
     * @param dataLen length of the data (header excluded)
     * @param nxtHop  the node from whom the packet came
     * @param fragNum fragmentation number
     */
    public Header(int source, int dest, boolean syn, boolean ack, boolean frag, boolean dm,
                  int seqNum, int dataLen, int nxtHop, int fragNum) {
        this.ack = ack;
        this.dataLen = dataLen;
        this.dest = dest;
        this.dm = dm;
        this.source = source;
        this.frag = frag;
        this.fragNum = fragNum;
        this.nxtHop = nxtHop;
        this.seqNum = seqNum;
        this.syn = syn;
    }

    /**
     * Creates a header byte array with all the information correctly set.
     *
     * @return a byte array with all bits set to the corresponding input
     */
    public byte[] toByteArray() {
        byte[] output = new byte[HEADER_LENGTH];
        String firstByte = createFirstHeaderByte(source, dest, syn, ack, frag, dm);
        output[0] = stringToByte(firstByte);
        String secondByte = createSecondHeaderByte(seqNum, dataLen);
        output[1] = stringToByte(secondByte);
        String thirdByte = createThirdHeaderByte(dataLen, nxtHop, fragNum);
        output[2] = stringToByte(thirdByte);
        return output;
    }

    /**
     * Creates a string of bits for the first byte of the header.
     *
     * @param dest destination node ID
     * @param syn  synchronization flag
     * @param ack  acknowledgement flag
     * @param frag fragmentation flag
     * @param dm   direct message flag
     * @return a string of bits for the first byte of the header
     */
    public static String createFirstHeaderByte(int nodeID, int dest,
                                               boolean syn, boolean ack, boolean frag, boolean dm) {
        StringBuilder firstByte = new StringBuilder();
        boolean[] flags = {syn, ack, frag, dm};
        firstByte.append(padString(Integer.toBinaryString(nodeID), 2));
        firstByte.append(padString(Integer.toBinaryString(dest), 2));
        for (boolean flag : flags) {
            firstByte.append(flag ? "1" : "0");
        }
        return firstByte.toString();
    }

    /**
     * Creates a string of bits for the second byte of the header.
     *
     * @param seq     sequence number for the packet
     * @param dataLen length of the actual data (so excluding the header)
     * @return a string of bits for the second byte of the header
     */
    public static String createSecondHeaderByte(int seq, int dataLen) {
        String output = "";
        output += padString(Integer.toBinaryString(seq), 5);
        String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
        output += dataLenString.substring(0, 3);
        return output;
    }

    /**
     * Creates a string of bits for the third byte of the header.
     *
     * @param dataLen length of the actual data (so excluding the header)
     * @param nxtHop  node ID from the node who actually sent the packet
     * @param fragNum fragmentation number to indicate which fragment this is
     * @return a string of bits for the third byte of the header
     */
    public static String createThirdHeaderByte(int dataLen, int nxtHop, int fragNum) {
        String output = "";
        String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
        output += dataLenString.substring(3);
        output += padString(Integer.toBinaryString(nxtHop), 2);
        output += padString(Integer.toBinaryString(fragNum), 4);
        return output;
    }

    /**
     * Formats a header into a string.
     *
     * @return formatted string of the header
     */
    public String toString() {
        return "source = " + source + ", " + "destination = " + dest + ", "
                + "syn-flag = " + syn + ", " + "ack-flag = " + ack + ", "
                + "frag-flag = " + frag + ", " + "ack-flag = " + ack + ", "
                + "direct message-flag = " + dm + ", " + "sequence number = " + seqNum + ", "
                + "data length = " + dataLen + ", " + "next hop = " + nxtHop + ", "
                + "fragmentation number = " + fragNum;
    }

    /**
     * Gets the source node ID.
     *
     * @return source node ID
     */
    public int getSource() {
        return source;
    }

    /**
     * Gets the sequence number.
     *
     * @return sequence number
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Gets the destination node ID.
     *
     * @return destination node ID
     */
    public int getDest() {
        return dest;
    }

    /**
     * Gets the length of the data.
     *
     * @return length of the data
     */
    public int getDataLen() {
        return dataLen;
    }

    /**
     * Gets the fragmentation number.
     *
     * @return fragmentation number
     */
    public int getFragNum() {
        return fragNum;
    }

    /**
     * Gets the node ID of whom the packet came.
     *
     * @return node ID of whom the packet came
     */
    public int getNxtHop() {
        return nxtHop;
    }

    /**
     * Gets the value of the fragmentation flag.
     *
     * @return value of the fragmentation flag
     */
    public boolean getFrag() {
        return frag;
    }

    /**
     * Gets the value of the acknowledgement flag.
     *
     * @return value of the acknowledgement flag
     */
    public boolean getAck() {
        return ack;
    }

    /**
     * Gets the value of the direct-message flag.
     *
     * @return value of the direct-message flag
     */
    public boolean getDm() {
        return dm;
    }

    /**
     * Gets the value of the synchronization flag.
     *
     * @return value of the synchronization flag
     */
    public boolean getSyn() {
        return syn;
    }
}
