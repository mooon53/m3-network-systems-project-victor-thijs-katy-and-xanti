package utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Class containing helper functions needed in other classes.
 */
public class HelpFunc {
    /**
     * Pads a string with 0's.
     *
     * @param input  the input string
     * @param length amount of 0's wanted to be added
     * @return the padded string
     */
    public static String padString(String input, int length) {
        StringBuilder inputBuilder = new StringBuilder(input);
        // simply add as many 0's as indicated with the length
        // to the beginning of the given input string
        while (inputBuilder.length() < length) {
            inputBuilder.insert(0, "0");
        }
        return inputBuilder.toString();
    }

    /**
     * Sets a certain bit in a byte.
     *
     * @param input the byte in which the bit needs to be changed
     * @param pos   position of the bit that needs to be changed
     * @param set   value to which the bit should be set
     * @return resulting byte
     */
    public static byte setBit(byte input, int pos, boolean set) {
        if (set) {
            return (byte) (input | (1 << (8 - pos)));
        } else {
            return (byte) (input & ~(1 << (8 - pos)));
        }
    }

    /**
     * Sets a certain bit in a byte to true.
     *
     * @param input the byte in which the bit needs to be changed
     * @param pos   position of the bit that needs to be changed
     * @return resulting byte
     */
    public static byte setBit(byte input, int pos) {
        return setBit(input, pos, true);
    }

    /**
     * Turns string into a byte.
     *
     * @param set string that needs to be converted
     * @return byte converted from the string
     */
    public static byte stringToByte(String set) {
        byte count = 0;
        for (int i = 0; i < set.length(); i++) {
            count += Character.getNumericValue(set.charAt(i)) * Math.pow(2, set.length() - (i + 1));
        }
        return count;
    }

    /**
     * Turns a byte array to a string.
     *
     * @param input     byte array that needs to be converted
     * @param addZeroes whether zeroes should be added to make 8 bit long bytes
     * @return string converted from the byte array
     */
    public static String bytesToString(byte[] input, boolean addZeroes) {
        StringBuilder string = new StringBuilder();
        // loop over all the bytes in the given array
        for (byte b : input) {
            BitSet bitset = BitSet.valueOf(new byte[]{b});
            int start;

            // if we want to add zeroes we start from 7 (so in total 8 bits),
            // otherwise we would want to start from the length of the BitSet
            if (addZeroes) {
                start = 7;
            } else {
                start = bitset.length() - 1;
            }

            for (int i = start; i >= 0; i--) {
                string.append(bitset.get(i) ? 1 : 0);
            }
            string.append(" ");
        }
        return string.toString();
    }

    /**
     * Turns a byte array into a string with added zeroes.
     *
     * @param input byte array that needs to be converted
     * @return string converted from the byte array with added zeroes
     */
    public static String bytesToString(byte[] input) {
        return bytesToString(input, true);
    }

    /**
     * Turns a byte into a string.
     *
     * @param input byte that needs to be converted to a string
     * @return string converted from the byte with added zeroes
     */
    public static String byteToString(byte input) {
        return bytesToString(new byte[]{input}, true);
    }
}
