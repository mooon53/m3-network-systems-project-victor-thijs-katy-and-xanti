package control;

import static utils.HelpFunc.*;

public class Ping {
	int sourceID;

	public Ping(int source) {
		this.sourceID = source;
	}

	public byte[] toBytes() {
		String source = padString(Integer.toBinaryString(sourceID), 2);
		String firstByte = source + "000000";
		String secondByte = "00000000";
		byte[] output = new byte[2];
		output[0] = stringToByte(firstByte);
		output[1] = stringToByte(secondByte);
		return output;
	}

	public int getSourceID() {
		return sourceID;
	}
}
