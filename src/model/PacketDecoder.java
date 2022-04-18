package model;

import control.*;
import view.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

import static utils.HelpFunc.*;
import static control.Client.*;

/**
 * Decodes a packet, converting all the bytes into the needed information.
 */
public class PacketDecoder implements Runnable {
	byte[] packet;
	String message;
	Header header;

	// TODO: decide if we want to remove these
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
		// separate every part of the header from the first byte
		String firstByte = byteToString(packet[0]);
		int source = Integer.valueOf(firstByte.substring(0, 2), 2);
		int dest = Integer.valueOf(firstByte.substring(2, 4), 2);
		boolean syn = Integer.parseInt(firstByte.substring(4, 5)) == 1;
		boolean ack = Integer.parseInt(firstByte.substring(5, 6)) == 1;
		boolean frag = Integer.parseInt(firstByte.substring(6, 7)) == 1;
		boolean dm = Integer.parseInt(firstByte.substring(7, 8)) == 1;

		// separate every part of the header from the second byte
		String secondByte = byteToString(packet[1]);
		int seqNum = Integer.valueOf(secondByte.substring(0, 5), 2);
		String dataLenPart = secondByte.substring(5, 8);

		// separate every part of the header from the third byte
		String thirdByte = byteToString(packet[2]);
		int dataLen = Integer.valueOf(dataLenPart + thirdByte.substring(0, 2), 2);
		int nxtHop = Integer.valueOf(thirdByte.substring(2, 4), 2);
		int fragNum = Integer.valueOf(thirdByte.substring(4, 8), 2);

		// create a header with all the abstracted information
		header = new Header(source, dest, syn, ack, frag, dm, seqNum, dataLen, nxtHop, fragNum);
		// abstract the actual text data from the packet
		byte[] messageBytes = new byte[dataLen];
		System.arraycopy(packet, 3, messageBytes, 0, dataLen);
		message = new String(messageBytes, StandardCharsets.UTF_8);
		handlePackage();
	}

	/**
	 * Handles a packet according to the information in the header.
	 */
	private void handlePackage() {
		if (MyProtocol.DEBUGGING_MODE) {
			System.out.println(bytesToString(packet));
			DebugInterface.printHeaderInformation(header);
		}
		// connect the package to a fragmentation handler
		FragHandler fragHandler = new FragHandler();
		if (fragHandlerExists(seqNum)) {
			fragHandler = getFragHandler(seqNum);
		}
		// TODO: make this check if we've received the message before
		// if the fragmentation handler does not already has this packet,
		// we create a fragment for it
		if (!fragHandler.hasFragment(fragNum)) {
			Fragment fragment = new Fragment(source, seqNum, fragNum, message);
			if (frag && !fragHandlerExists(seqNum)) {
				// if it is a fragment and the handler does not exist,
				// then make a new fragment for it
				fragHandler = new FragHandler(fragment);
				Thread fragHandlerThread = new Thread(fragHandler);
				fragHandlerThread.start();
				addFragHandler(seqNum, fragHandler);
			} else if (frag) {
				// else if it is a fragment and the handler already exists,
				// then pass it to that fragHandler
				getFragHandler(seqNum).addFragment(fragment);
			} else if (!dm) {
				// if it is not a fragment and not a direct message,
				// then we can make it a full message and print it
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
		// copy the input string into the byte buffer
		toSend.put(packet, 0, dataLen + 3);
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

	/**
	 * Gets the header object from the packetDecoder.
	 *
	 * @return header object from the packetDecoder
	 */
	public Header getHeader() throws InterruptedException {
		return header;
	}
}
