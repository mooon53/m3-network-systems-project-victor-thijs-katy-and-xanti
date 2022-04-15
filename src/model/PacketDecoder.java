package model;

import control.*;
import view.UI;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static utils.HelpFunc.*;
import static control.Client.*;
import static control.MyProtocol.*;

public class PacketDecoder implements Runnable {
	byte[] packet;
	int source;
	int dest;
	boolean syn;
	boolean ack;
	boolean frag;
	boolean DM;
	int seqNum;
	int dataLen;
	int nxtHop;
	int fragNum;
	String message;

	public PacketDecoder(byte[] packet) {
		this.packet = packet;
	}

	public void run() {
		System.out.print(bytesToString(packet[0]));
		System.out.print(bytesToString(packet[1]));
		System.out.println(bytesToString(packet[2]));
		String firstByte = bytesToString(packet[0]);
		source = Integer.valueOf(firstByte.substring(0, 2), 2);
		dest = Integer.valueOf(firstByte.substring(2, 4), 2);
		syn = Integer.parseInt(firstByte.substring(4, 5)) == 1;
		ack = Integer.parseInt(firstByte.substring(5, 6)) == 1;
		frag = Integer.parseInt(firstByte.substring(6, 7)) == 1;
		DM = Integer.parseInt(firstByte.substring(7, 8)) == 1;
		String secondByte = bytesToString(packet[1]);
		seqNum = Integer.valueOf(secondByte.substring(0, 5), 2);
		String dataLenPart = secondByte.substring(5, 8);
		System.out.println("datalenpart "+dataLenPart);
		String thirdByte = bytesToString(packet[2]);
		dataLen = Integer.valueOf(dataLenPart + thirdByte.substring(0, 2), 2);
		System.out.println("datalen bits "+thirdByte.substring(0, 2) + dataLenPart);
		nxtHop = Integer.valueOf(thirdByte.substring(2, 4), 2);
		fragNum = Integer.valueOf(thirdByte.substring(4, 8), 2);
		byte[] messageBytes = new byte[dataLen];
		System.out.println("datalength "+dataLen);
		System.arraycopy(packet, 3, messageBytes, 0, dataLen);
		message = new String(messageBytes, StandardCharsets.UTF_8);
		handleMessage();
	}

	private void handleMessage() {
		//System.out.println("test");
		FragHandler fragHandler = new FragHandler();
		if (fragHandlerExists(seqNum)) {
			fragHandler = getFragHandler(seqNum);
		}
		if (!fragHandler.hasFragment(fragNum)) { // TODO: make this check if we've received the message before
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
		if (true) {// TODO: add a condition to check if all reachable nodes have received this packet
			sendMessage();
		}
	}

	private void sendMessage(boolean ack) {
		if (ack) {
			packet[0] = setBit(packet[0], 5);
		}
		ByteBuffer toSend = ByteBuffer.allocate(dataLen+3); // make a new byte buffer with the length of the input string
		toSend.put(packet, 0, dataLen+3); // copy the input string into the byte buffer.
		Packet msg;
		msg = new Packet(PacketType.DATA, toSend);
		MyProtocol.sendMessage(msg);
	}

	private void sendMessage() {
		sendMessage(true);
	}
}
