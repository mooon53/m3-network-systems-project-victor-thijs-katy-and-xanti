package tests;

import control.Fragment;
import control.Header;
import model.FragHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.HelpFunc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods from Header.
 */
public class HelpFuncTest {
    @Test
    void testPadString() {
        assertEquals("00001010", HelpFunc.padString("1010", 8));
    }

    @Test
    void testSetBit() {
        assertEquals(0, HelpFunc.setBit((byte) 1, 8, false));
        assertEquals(1, HelpFunc.setBit((byte) 0, 8));
    }

    @Test
    void testStringToByte() {
        assertEquals((byte) 64, HelpFunc.stringToByte("01000000"));
    }

    @Test
    void testBytesToString() {
        assertEquals("00000011 ", HelpFunc.bytesToString(new byte[]{3}));
        assertEquals("00000011 00000010 ", HelpFunc.bytesToString(new byte[]{3, 2}));
        assertEquals("11 ", HelpFunc.bytesToString(new byte[]{3}, false));
    }
}