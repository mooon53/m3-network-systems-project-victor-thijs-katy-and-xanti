import java.util.BitSet;
import java.util.Random;

public class Test {
	private static int source = 1;

	private static int nodeID = 0;

	private static int currentSeqNum = 27;

	public static void main(String[] args) {
		byte[] header = createHeader(0, false, false, true, false, currentSeqNum, 7, 1, 2);
		System.out.println(bytesToString(header, true));
	}

	public static byte[] createHeader(int dest, boolean syn, boolean ack, boolean frag, boolean DM, int seq, int dataLen, int nxtHop, int fragNum) {
			byte[] output = new byte[3];
			String firstByte = createFirstHeaderByte(dest, syn, ack, frag, DM);
			System.out.println(firstByte);
			System.out.println(setByte(firstByte));
			output[0] = setByte(firstByte);
			String secondByte = createSecondHeaderByte(seq, dataLen);
			System.out.println(secondByte);
			System.out.println(setByte(secondByte));
			output[1] = setByte(secondByte);
			String thirdByte = createThirdHeaderByte(dataLen, nxtHop, fragNum);
			System.out.println(thirdByte);
			System.out.println(setByte(thirdByte));
			output[2] = setByte(thirdByte);
			return output;
		}

		public static String createFirstHeaderByte(int dest, boolean syn, boolean ack, boolean frag, boolean DM) {
			String firstByte = "";
			boolean[] flags = {syn, ack, frag, DM};
			firstByte += padString(Integer.toBinaryString(nodeID), 2);
			firstByte += padString(Integer.toBinaryString(dest), 2);
			for (boolean flag : flags) {
				firstByte += flag ? "1" : "0";
			}
			return firstByte;
		}

		public static String createSecondHeaderByte(int seq, int dataLen) {
			String output = "";
			output += padString(Integer.toBinaryString(seq), 5);
			String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
			output += dataLenString.substring(0, 3);
			return output;
		}

		public static String createThirdHeaderByte(int dataLen, int nxtHop, int fragNum) {
			String output = "";
			String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
			output += dataLenString.substring(3);
			output += padString(Integer.toBinaryString(nxtHop), 2);
			output += padString(Integer.toBinaryString(fragNum), 4);
			return output;
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
        for(int i = 0; i < set.length(); i++) {
            count += Character.getNumericValue(set.charAt(i)) * Math.pow(2, set.length() - (i+1));
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
}