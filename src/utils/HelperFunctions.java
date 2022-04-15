package utils;

import control.Packet;
import java.util.BitSet;

public class HelperFunctions {
    public byte bitsetToByte(BitSet bitset) {
        if (bitset.length() > 8) return 0;
        return bitset.toByteArray()[0];
    }

    public BitSet bytesToBitSet(byte[] input) {
        return BitSet.valueOf(input);
    }

    public byte xorCheck(Packet packet) {
        byte[] bytes = packet.getData().array();
        if (bytes.length >= 2) {
            byte xoredByte = bytes[0];
            for (int i = 1; i < bytes.length; i++) {
                xoredByte = (byte) (xoredByte ^ bytes[i]);
            }
            return xoredByte;
        } else if (bytes.length == 1) {
            return bytes[0];
        } else {
            return 0;
        }
    }

    public byte[] concatByteArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);

        return result;
    }

    public BitSet concatBitSet(BitSet[] bitsets) {
        BitSet output = new BitSet();
        int index = 0;
        for (int i = 0; i < bitsets.length; i++) {
            if (i > 0) index += bitsets[i - 1].length();
            for (int n = 0; n < bitsets[i].length(); i++) {
                if (bitsets[i].get(n)) output.set(index + n);
            }
        }
        return output;
    }

    private byte getBit(byte b, int index) {
        return (byte) ((b >> 7 - index) & 1);
    }
}
