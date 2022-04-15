package utils;

import control.Packet;

import java.util.BitSet;

public class HelpFunc {
	public static byte bitsetToByte(BitSet bitset) {
		if (bitset.length() > 8) return 0;
		return bitset.toByteArray()[0];
	}

	public static BitSet bytesToBitSet(byte[] input) {
		return BitSet.valueOf(input);
	}

	public static byte xorCheck(Packet packet) {
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

	public static byte[] concatByteArrays(byte[] array1, byte[] array2) {
		byte[] result = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);

		return result;
	}

	public static BitSet concatBitSet(BitSet[] bitsets) {
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

	public static byte getBit(byte b, int index) {
		return (byte) ((b >> 7 - index) & 1);
	}

	public static String padString(String input, int length) {
		while (input.length() < length) {
			input = "0" + input;
		}
		return input;
	}

	public static byte setBit(byte input, int pos, boolean set) {
		if (set) {
			return (byte) (input | (1 << (8 - pos)));
		} else {
			return (byte) (input & ~(1 << (8 - pos)));
		}
	}

	public static byte setBit(byte input, int pos) {
		return setBit(input, pos, true);
	}

	public static boolean isSet(byte input, int pos) {
		int mask = 1 << (8 - pos);
		return (input & mask) == mask;
	}

	public static byte setByte(String set) {
		byte count = 0;
		for (int i = 0; i < set.length(); i++) {
			count += Character.getNumericValue(set.charAt(i)) * Math.pow(2, set.length() - (i + 1));
		}
		return count;
	}

	public static String bytesToString(byte[] input, boolean addZeroes) {
		String string = "";
		for (byte b : input) {
			BitSet bitset = BitSet.valueOf(new byte[]{b});
			int start;

			if (addZeroes) {
				start = 7;
			} else {
				start = bitset.length() - 1;
			}

			for (int i = start; i >= 0; i--) {
				string += bitset.get(i) ? 1 : 0;
			}
			string += " ";
		}
		return string;
	}

	public static String bytesToString(byte[] input) {
		return bytesToString(input, true);
	}

	public static String bytesToString(byte input) {
		return bytesToString(new byte[]{input}, true);
	}
}
