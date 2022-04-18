package utils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Class containing helper functions needed in other classes.
 */
public class HelpFunc {
    /**
     * Prints a given ByteBuffer.
     *
     * @param bytes       ByteBuffer which needs to be printed
     * @param bytesLength length of the ByteBuffer
     */
    public static void printByteBuffer(ByteBuffer bytes, int bytesLength) {
        // prints out the bytes in a ByteBuffer
        System.out.print("DATA: ");
        for (int i = 0; i < bytesLength; i++) {
            System.out.print(bytes.get(i) + " ");
        }
        System.out.println();
    }

    /**
     * Takes a bitset and makes it a byte.
     *
     * @param bitset bitset that needs to be converted
     * @return byte corresponding to the given bitset
     */
    public static byte bitsetToByte(BitSet bitset) {
        // simply converts every bitset that is smaller or equal to 8 bits to a byte
        if (bitset.length() > 8) {
            return 0;
        }
        return bitset.toByteArray()[0];
    }

    /**
     * Takes a byte array and makes it a bitset.
     *
     * @param input byte array that needs to be converted
     * @return bitset corresponding to the given byte array
     */
    public static BitSet bytesToBitSet(byte[] input) {
        return BitSet.valueOf(input);
    }


    /**
     * Concatenates bitsets to one bitset.
     *
     * @param bitsets bitsets that need to be concatenated
     * @return concatenated bitset
     */
    public static BitSet concatBitSet(BitSet[] bitsets) {
        BitSet output = new BitSet();
        int index = 0;
        for (int i = 0; i < bitsets.length; i++) {
            if (i > 0) {
                index += bitsets[i - 1].length();
            }
            for (int n = 0; n < bitsets[i].length(); n++) {
                if (bitsets[i].get(n)) {
                    output.set(index + n);
                }
            }
        }
        return output;
    }

    /**
     * Gets the bit of a byte at a certain index.
     *
     * @param b     byte in which the bit is located
     * @param index location of the wanted bit
     * @return the bit in the byte at the wanted location
     */
    public static byte getBit(byte b, int index) {
        return (byte) ((b >> 7 - index) & 1);
    }

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
     * Checks if a certain bit in a byte is true.
     *
     * @param input byte that needs to be checked
     * @param pos   position of the bit that needs to be set to true
     * @return true if the certain bit in the byte is true
     */
    public static boolean isSet(byte input, int pos) {
        int mask = 1 << (8 - pos);
        return (input & mask) == mask;
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
