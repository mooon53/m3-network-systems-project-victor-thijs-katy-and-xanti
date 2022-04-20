package model;

import control.Header;

import java.util.Arrays;

import static control.MyProtocol.DEBUGGING_MODE;

/**
 * Encodes a packet which needs to be sent.
 */
public class PacketEncoder {
	private byte[] data;
	private byte[][] fragmented;
	private int dataLength;
	private Header standardHeader;

	/**
	 * Returns the message as a fragmented byte arrays.
	 *
	 * @return message as a fragmented byte arrays
	 */
	public byte[][] fragmentedMessage() {
		int numPackets = numPackets(dataLength);
		fragmented = new byte[numPackets][];

		if (numPackets == 1) {
			standardHeader.setDataLen(dataLength);
			fragmented[0] = new byte[3 + dataLength];
			System.arraycopy(standardHeader.toByteArray(), 0, fragmented[0], 0, 3);
			System.arraycopy(data, 0, fragmented[0], 3, dataLength);
			return fragmented;
		}
		standardHeader.setFrag(true);
		if (DEBUGGING_MODE) System.out.println(Arrays.toString(data));

		for (int i = 0; i < dataLength + 1; i += 29) {
			int packetDataLength = 29;
			boolean lastPacket = dataLength + 1 - i < 29;
			byte[] packet;
			if (lastPacket) {
				standardHeader.setDataLen(dataLength + 1 - i);
				packetDataLength = dataLength - i;
				packet = new byte[packetDataLength + 1 + 3];
			} else {
				standardHeader.setDataLen(29);
				packet = new byte[32];
			}
			standardHeader.setFragNum(i / 29);
			System.arraycopy(standardHeader.toByteArray(), 0, packet, 0, 3);
			System.arraycopy(data, i, packet, 3, packetDataLength);
			if (lastPacket) packet[packet.length - 1] = 0x03;
			if (DEBUGGING_MODE) System.out.println(Arrays.toString(packet));
			fragmented[standardHeader.getFragNum()] = packet;
		}
		return fragmented;
	}

	/**
	 * Constructor for the packet encoder.
	 *
	 * @param data           data which needs to be encoded
	 * @param standardHeader a standard header for all fragments
	 */
	public PacketEncoder(byte[] data, Header standardHeader) {
		this.data = data;
		dataLength = data.length;
//        if (data.length > 29) {
//            byte[] dataFrag = new byte[data.length + 1];
//            System.arraycopy(data, 0, dataFrag, 0, data.length);
//            dataFrag[dataFrag.length - 1] = 0x03;
//            dataLength++;
//            this.data = dataFrag;
//        }
		this.standardHeader = standardHeader;
	}

	/**
	 * Returns how many fragments should be made for the given data.
	 *
	 * @param n the data length
	 * @return how many fragments are needed for a certain amount of data
	 */
	private int numPackets(int n) {
//        int extra = 1;
//        if (n % 29 == 0) {
//            extra = 0;
//        }
//        return (n - n % 29) / 29 + extra;
		if (n <= 29) return 1;
		double amount = n + 1; // add 1 for end of text character
		return (int) ((amount / 29) + 0.999); // divide by 29 to get the amount of packets, by adding 0.999 it will always round up except for with whole numbers
	}
}
