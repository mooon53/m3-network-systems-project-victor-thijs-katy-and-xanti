package model;

import control.Client;

public class PacketHandler extends Thread {
	byte[] message;
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

	public PacketHandler(byte[] message) {
		this.message = message;
	}

	public void run() {
		String firstByte = Client.bytesToString(message[0]);
		source = Integer.valueOf(firstByte.substring(0,2),2);
		dest = Integer.valueOf(firstByte.substring(2,4),2);
		syn = Integer.parseInt(firstByte.substring(4,5))==1;
		ack = Integer.parseInt(firstByte.substring(5,6))==1;
		frag = Integer.parseInt(firstByte.substring(6,7))==1;
		DM = Integer.parseInt(firstByte.substring(7,8))==1;
		String secondByte = Client.bytesToString(message[1]);
		seqNum = Integer.valueOf(secondByte.substring(0,5), 2);
		String dataLenPart = secondByte.substring(5,8);
		String thirdByte = Client.bytesToString(message[2]);
		dataLen = Integer.valueOf(thirdByte.substring(0,2)+dataLenPart, 2);
		nxtHop = Integer.valueOf(thirdByte.substring(2,4), 2);
		fragNum = Integer.valueOf(thirdByte.substring(4,8), 2);
	}
}
