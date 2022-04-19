package control;

/**
 * Message object which is a complete message.
 */
public class Message {
    private String message;
    private int sourceID;

    /**
     * Constructor for a message.
     *
     * @param source  the source's node ID of the sender of the message
     * @param message the actual text from the message
     */
    public Message(int source, String message) {
        this.sourceID = source;
        this.message = message;
    }

    /**
     * Constructor for a message with a fragment as input.
     * @param fragment fragment for which a message should be created.
     */
    public Message(Fragment fragment) {
        this.sourceID = fragment.getHeader().getSource();
        this.message = fragment.getMessagePart();
    }

    /**
     * Takes the information from a message and turns it to a layout string.
     *
     * @return actual text from the message
     */
    public String toString() {
        return message;
    }

    /**
     * Gets the source's node ID of the sender of the message.
     *
     * @return source's node ID of the sender of the message
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * Removes a character from the actual data text.
     * @param character character that should be removed
     */
    public void removeCharacter(String character) {
        message = message.replace(character, "");
    }
}
