package tests;


import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import static utils.HelpFunc.*;

@SuppressWarnings("InvisibleCharacter")
public class Test {
	boolean test = true;

	public static void main(String[] args) {
//		byte b = 0x03;
//		String test = new String(new byte[]{b}, StandardCharsets.UTF_8);
//		System.out.println(test);
//		byte test2 = test.getBytes(StandardCharsets.UTF_8)[0];
//		boolean bool = test.endsWith("");
//		System.out.println(bool);
//		test ="";
//		System.out.println(test.endsWith(""));
//
//		System.out.println(bytesToString(new byte[]{0x00, 0x01}, false));
//
//		int[][] arrayArray = new int[3][2];
//		arrayArray[0] = new int[]{1, 1, 1, 1};
//		for (int[] array : arrayArray) {
//			System.out.println("ayayay");
//			for (int integer : array) {
//				System.out.println(integer);
//			}
//		}
		String[] numbers = new String[]{"00000000", "01000000", "10000000", "11000000"};
		for (int i = 0; i < 4; i++) {
			byte b = stringToByte(numbers[i]);
			String s = byteToString(b).substring(0, 2);
			b = stringToByte(s);
			System.out.println(b);
		}
	}

	public static String bytesToString(byte[] input, boolean addZeroes) {
		StringBuilder string = new StringBuilder();
		for (byte b : input) {
			BitSet bitset = BitSet.valueOf(new byte[]{b});
			int start;

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
}