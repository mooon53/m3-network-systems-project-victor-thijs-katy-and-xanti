package tests;

import control.Fragment;
import control.Header;
import model.FragHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods from Header.
 */
public class HeaderTest {
    private Header header;

    @BeforeEach
    void setUp() {
        header = new Header();
    }

    @Test
    void testToByteArrayEmptyHeader() {
        assertEquals(0, header.toByteArray()[0]);
        assertEquals(0, header.toByteArray()[1]);
        assertEquals(0, header.toByteArray()[2]);
    }

    @Test
    void testToByteArray() {
        header.setDm();
        header.setDataLen(4);
        header.setFragNum(1);

        assertEquals(1, header.toByteArray()[0]);
        assertEquals(1, header.toByteArray()[1]);
        assertEquals(1, header.toByteArray()[2]);
    }

    @Test
    void testToString() {
        header.setFrag();
        header.setAck();
        header.setDataLen(10);

        String string = "source = " + 0 + ", " + "destination = " + 0 + ", "
                + "syn-flag = " + false + ", " + "ack-flag = " + true + ", "
                + "frag-flag = " + true + ", " + "ack-flag = " + true + ", "
                + "direct message-flag = " + false + ", " + "sequence number = " + 0 + ", "
                + "data length = " + 10 + ", " + "next hop = " + 0 + ", "
                + "fragmentation number = " + 0;

        assertEquals(string, header.toString());
    }

}