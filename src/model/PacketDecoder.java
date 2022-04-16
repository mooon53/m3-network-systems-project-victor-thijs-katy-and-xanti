package model;

import control.*;
import view.UI;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static utils.HelpFunc.*;
import static control.Client.*;

/**
 * Decodes a packet, converting all the bytes into the needed information.
 */
public class PacketDecoder implements Runnable {
	byte[] packet;
	String message;
	Header header;

	// TODO: decide if we want to remove this
	int source;
	int dest;
	boolean syn;
	boolean ack;
	boolean frag;
	boolean dm;
	int seqNum;
	int dataLen;
	int nxtHop;
	int fragNum;

	/**
	 * Constructor for the packet decoder.
	 *
	 * @param packet byte array which needs to be decoded
	 */
	public PacketDecoder(byte[] packet) {
		this.packet = packet;
	}

	/**
	 * Runs when called decoding a packet.
	 */
	public void run() {
		String firstByte = byteToString(packet[0]);
		int source = Integer.valueOf(firstByte.substring(0, 2), 2);
		int dest = Integer.valueOf(firstByte.substring(2, 4), 2);
		boolean syn = Integer.parseInt(firstByte.substring(4, 5)) == 1;
		boolean ack = Integer.parseInt(firstByte.substring(5, 6)) == 1;
		boolean frag = Integer.parseInt(firstByte.substring(6, 7)) == 1;
		boolean dm = Integer.parseInt(firstByte.substring(7, 8)) == 1;

		String secondByte = byteToString(packet[1]);
		int seqNum = Integer.valueOf(secondByte.substring(0, 5), 2);
		String dataLenPart = secondByte.substring(5, 8);

		String thirdByte = byteToString(packet[2]);
		int dataLen = Integer.valueOf(dataLenPart + thirdByte.substring(0, 2), 2);
		int nxtHop = Integer.valueOf(thirdByte.substring(2, 4), 2);
		int fragNum = Integer.valueOf(thirdByte.substring(4, 8), 2);

		header = new Header(source, dest, syn, ack, frag, dm, seqNum, dataLen, nxtHop, fragNum);

		byte[] messageBytes = new byte[dataLen];
		System.arraycopy(packet, 3, messageBytes, 0, dataLen);
		message = new String(messageBytes, StandardCharsets.UTF_8);
		handlePackage();
	}

	/**
	 * Handles a packet according to the information in the header.
	 */
	private void handlePackage() {
		FragHandler fragHandler = new FragHandler();
		if (fragHandlerExists(seqNum)) {
			fragHandler = getFragHandler(seqNum);
		}
		// TODO: make this check if we've received the message before
		if (!fragHandler.hasFragment(fragNum)) {
			Fragment fragment = new Fragment(source, seqNum, fragNum, message);
			if (frag && !fragHandlerExists(seqNum)) {
				fragHandler = new FragHandler(fragment);
				Thread fragHandlerThread = new Thread(fragHandler);
				fragHandlerThread.start();
				addFragHandler(seqNum, fragHandler);
			} else if (frag) {
				getFragHandler(seqNum).addFragment(fragment);
			} else {
				Message fullMessage = new Message(source, message);
				UI.printMessage(fullMessage);
			}
		}
		// TODO: add a condition to check if all reachable nodes have received this packet
		if (true) {
			sendPacket();
		}
	}

	/**
	 * Forwards a packet which needs to be forwarded to MyProtocol.
	 *
	 * @param ack whether the packet is an acknowledgement
	 */
	private void sendPacket(boolean ack) {
		if (ack) {
			packet[0] = setBit(packet[0], 5);
		}
		// make a new byte buffer with the length of the input string
		ByteBuffer toSend = ByteBuffer.allocate(dataLen + 3);
		toSend.put(packet, 0, dataLen + 3); // copy the input string into the byte buffer.
		Packet msg;
		msg = new Packet(PacketType.DATA, toSend);
		MyProtocol.sendMessage(msg);
	}

	/**
	 * Forwards a packet which needs to be forwarded to MyProtocol
	 * with its acknowledgment flag set to true as a standard.
	 */
	private void sendPacket() {
		sendPacket(true);
	}

	public Header getHeader() {
		return header;
	}
}
