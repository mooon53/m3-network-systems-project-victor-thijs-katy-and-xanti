package model.objects;

/**
 * Fragment object which is a fragment of a message.
 */
public class Fragment {
    Header header;
    private String messagePart;

    /**
     * Constructor for a fragment of a message.
     *
     * @param header  the header
     * @param message the header
     */
    public Fragment(Header header, String message) {
        this.header = header;
        this.messagePart = message;
    }

    /**
     * Constructor for a fragment with an empty header.
     *
     * @param message
     */
    public Fragment(String message) {
        new Fragment(new Header(), message);
    }

    /**
     * Gets the header from the fragment object.
     *
     * @return header from the fragment object
     */
    public Header getHeader() {
        return header;
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
