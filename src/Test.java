import control.Client;

public class Test {
	private static int source = 1;

	private static int nodeID = 2;

	private static int currentSeqNum = 27;

	static byte[] message = createHeader(3, false, true, false, false, 24, 14, 1, 0);

	public static void main(String[] args) {
		String firstByte = Client.bytesToString(message[0]);
		System.out.print(Integer.valueOf(firstByte.substring(0,2),2));
		System.out.print(Integer.valueOf(firstByte.substring(2,4),2));
		System.out.print(Integer.parseInt(firstByte.substring(4,5))==1);
		System.out.print(Integer.parseInt(firstByte.substring(5,6))==1);
		System.out.print(Integer.parseInt(firstByte.substring(6,7))==1);
		System.out.print(Integer.parseInt(firstByte.substring(7,8))==1);
		System.out.println();
		String secondByte = Client.bytesToString(message[1]);
		System.out.print(Integer.valueOf(secondByte.substring(0,5), 2));
		String dataLenPart = secondByte.substring(5,8);
		String thirdByte = Client.bytesToString(message[2]);
		System.out.print(Integer.valueOf(dataLenPart+thirdByte.substring(0,2), 2));
		System.out.println(thirdByte.substring(0,2)+dataLenPart);
		System.out.println();
		System.out.print(Integer.valueOf(thirdByte.substring(2,4), 2));
		System.out.print(Integer.valueOf(thirdByte.substring(4,8), 2));
	}

	public static byte[] createHeader(int dest, boolean syn, boolean ack, boolean frag, boolean DM, int seq,
		                           int dataLen, int nxtHop, int fragNum) {
			byte[] output = new byte[3];
			String firstByte = createFirstHeaderByte(dest, syn, ack, frag, DM);
			output[0] = setByte(firstByte);
			String secondByte = createSecondHeaderByte(seq, dataLen);
			output[1] = setByte(secondByte);
			String thirdByte = createThirdHeaderByte(dataLen, nxtHop, fragNum);
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
			System.out.println(dataLenString);
			output += dataLenString.substring(0, 3);
			return output;
		}

		public static String createThirdHeaderByte(int dataLen, int nxtHop, int fragNum) {
			String output = "";
			String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
			System.out.println(dataLenString);
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

	public static String bytesToString(byte[] input) {
			return bytesToString(input, true);
	}

	public static String bytesToString(byte input) {
			return bytesToString(new byte[]{input}, true);
	}
}