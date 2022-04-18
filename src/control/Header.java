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
        this.source = source;
        this.dest = dest;
        this.syn = syn;
        this.ack = ack;
        this.frag = frag;
        this.dm = dm;
        this.seqNum = seqNum;
        this.dataLen = dataLen;
        this.nxtHop = nxtHop;
        this.fragNum = fragNum;
    }

    /**
     * Header constructor with everything set to standard.
     */
    public Header() {
        new Header(0, 0, false, false, false, false, 0, 0, 0, 0);
    }

    /**
     * Creates a header byte array with all the information correctly set.
     *
     * @return a byte array with all bits set to the corresponding input
     */
    public byte[] toByteArray() {
        // for the structure of the header we refer to our documentation
        byte[] output = new byte[HEADER_LENGTH];
        String firstByte = createFirstHeaderByte();
        output[0] = stringToByte(firstByte);
        String secondByte = createSecondHeaderByte();
        output[1] = stringToByte(secondByte);
        String thirdByte = createThirdHeaderByte();
        output[2] = stringToByte(thirdByte);
        return output;
    }

    /**
     * Creates a string of bits for the first byte of the header.
     *
     * @param destID   destination node ID
     * @param synFlag  synchronization flag
     * @param ackFlag  acknowledgement flag
     * @param fragFlag fragmentation flag
     * @param dmFlag   direct message flag
     * @return a string of bits for the first byte of the header
     */
    public String createFirstHeaderByte(int nodeID, int destID, boolean synFlag, boolean ackFlag,
                                        boolean fragFlag, boolean dmFlag) {
        StringBuilder firstByte = new StringBuilder();
        boolean[] flags = {synFlag, ackFlag, fragFlag, dmFlag};
        firstByte.append(padString(Integer.toBinaryString(nodeID), 2));
        firstByte.append(padString(Integer.toBinaryString(destID), 2));
        for (boolean flag : flags) {
            firstByte.append(flag ? "1" : "0");
        }
        return firstByte.toString();
    }

    /**
     * Creates a string of bits for the first byte of the header with the stored instance variables.
     *
     * @return a string of bits for the first byte of the header
     */
    private String createFirstHeaderByte() {
        return createFirstHeaderByte(source, dest, syn, ack, frag, dm);
    }

    /**
     * Creates a string of bits for the second byte of the header.
     *
     * @param seq        sequence number for the packet
     * @param dataLength length of the actual data (so excluding the header)
     * @return a string of bits for the second byte of the header
     */
    public String createSecondHeaderByte(int seq, int dataLength) {
        String output = "";
        output += padString(Integer.toBinaryString(seq), 5);
        String dataLenString = padString(Integer.toBinaryString(dataLength), 5);
        output += dataLenString.substring(0, 3);
        return output;
    }

    /**
     * Creates a string of bits for the second byte of the header
     * with the stored instance variables.
     *
     * @return a string of bits for the second byte of the header
     */
    private String createSecondHeaderByte() {
        return createSecondHeaderByte(seqNum, dataLen);
    }

    /**
     * Creates a string of bits for the third byte of the header.
     *
     * @param dataLength length of the actual data (so excluding the header)
     * @param nextHop    node ID from the node who actually sent the packet
     * @param fragNumber fragmentation number to indicate which fragment this is
     * @return a string of bits for the third byte of the header
     */
    public String createThirdHeaderByte(int dataLength, int nextHop, int fragNumber) {
        String output = "";
        String dataLenString = padString(Integer.toBinaryString(dataLength), 5);
        output += dataLenString.substring(3);
        output += padString(Integer.toBinaryString(nextHop), 2);
        output += padString(Integer.toBinaryString(fragNumber), 4);
        return output;
    }

    /**
     * Creates a string of bits for the third byte of the header with the stored instance variables.
     *
     * @return a string of bits for the third byte of the header
     */
    private String createThirdHeaderByte() {
        return createThirdHeaderByte(dataLen, nxtHop, fragNum);
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
     * Sets the source node ID.
     *
     * @param source the value the source node ID should be set to
     */
    public void setSource(int source) {
        this.source = source;
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
     * Set the sequence number.
     *
     * @param seqNum the value the sequence number should be set to
     */
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
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
     * Sets the destination node ID.
     *
     * @param dest the value the destination node ID should be set to
     */
    public void setDest(int dest) {
        this.dest = dest;
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
     * Sets the length of the data.
     *
     * @param dataLen the value the length of the data should be set to
     */
    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
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
     * Set the fragmentation number.
     *
     * @param fragNum the value the fragmentation number should be set to
     */
    public void setFragNum(int fragNum) {
        this.fragNum = fragNum;
    }

    /**
     * gets the node ID for the next hop.
     *
     * @return node ID for the next hop
     */
    public int getNxtHop() {
        return nxtHop;
    }

    /**
     * Sets the node ID for the next hop.
     *
     * @param nxtHop the value the node ID for the next hop should be set to
     */
    public void setNxtHop(int nxtHop) {
        this.nxtHop = nxtHop;
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
     * Sets the value of the fragmentation flag.
     *
     * @param frag the value the fragmentation flag should be set to
     */
    public void setFrag(boolean frag) {
        this.frag = frag;
    }

    /**
     * Sets the value of the fragmentation flag to true.
     */
    public void setFrag() {
        setFrag(true);
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
     * Sets the value of the acknowledgement flag.
     *
     * @param ack the value the acknowledgement flag should be set to
     */
    public void setAck(boolean ack) {
        this.ack = ack;
    }

    /**
     * Sets the value of the acknowledgement flag to true.
     */
    public void setAck() {
        setAck(true);
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
     * Sets the value of the direct message flag.
     *
     * @param dm the value the direct message flag should be set to
     */
    public void setDm(boolean dm) {
        this.dm = dm;
    }

    /**
     * Sets the value of the direct message flag to true.
     */
    public void setDm() {
        setDm(true);
    }

    /**
     * Gets the value of the synchronization flag.
     *
     * @return value of the synchronization flag
     */
    public boolean getSyn() {
        return syn;
    }

    /**
     * Sets the value of the synchronization flag.
     *
     * @param syn the value the synchronization flag should be set to
     */
    public void setSyn(boolean syn) {
        this.syn = syn;
    }

    /**
     * Sets the synchronization flag to true.
     */
    public void setSyn() {
        setSyn(true);
    }
}
