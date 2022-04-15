package control;

/**
 * Enum for all the different packet types.
 */
public enum PacketType {
    FREE,
    BUSY,
    DATA,
    SENDING,
    DONE_SENDING,
    DATA_SHORT,
    END,
    HELLO,
    SETUP
}