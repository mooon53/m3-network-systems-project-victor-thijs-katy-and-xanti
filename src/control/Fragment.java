package control;

/**
 * Fragment object which is a fragment of a message.
 */
public class Fragment {
    private int sourceID;
    private int seqNum;
    private int fragID;
    private boolean fragmented;
    private String messagePart;

    /**
     * Constructor for a fragment of a message.
     *
     * @param sourceID ID of the source node who sent the message
     * @param seqNum   sequence number of the message
     * @param fragID   fragmentation number of the packet
     * @param message  string containing the decoded message data
     */
    public Fragment(int sourceID, int seqNum, int fragID, boolean fragged, String message) {
        this.sourceID = sourceID;
        this.seqNum = seqNum;
        this.fragID = fragID;
        this.fragmented = fragged;
        this.messagePart = message;
    }

    public Fragment(int sourceID, int seqNum, int fragID, String message) {
        new Fragment(sourceID, seqNum, fragID, true, message);
    }

    /**
     * Gets the ID of the source node of the fragment.
     *
     * @return ID of the source node of the fragment
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * Gets the sequence number of the fragment.
     *
     * @return sequence number of the fragment
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Gets the fragmentation number of the fragment.
     *
     * @return fragmentation number of the fragment
     */
    public int getFragID() {
        return fragID;
    }

    public boolean isFragmented() {
        return fragmented;
    }

    /**
     * Gets the actual message contained in the fragment.
     *
     * @return actual message contained in the fragment
     */
    public String getMessagePart() {
        return messagePart;
    }
}
