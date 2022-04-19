package control;

import model.FragHandler;
import view.UI;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static control.Client.*;
import static control.PacketType.DATA;
import static utils.HelpFunc.*;
import static control.MyProtocol.*;

/**
 * Class that stores all the packets we have received, removing them once everyone in our range has seen them.
 * Also decides whether to retransmit etc.
 */
public class PacketStorage {
	// A map that maps the Packet to a combination of the sequence number and fragmentation number
	// For example, sequence 15 and fragmentation 4 would be 01111 00100 = 484
	// An array so we can store the packet from each sender in their own map
	private HashMap[] packets;
	private HashMap[] fragments;
	private HashMap[] received;

	private Client client;

	public PacketStorage(Client client) {
		this.client = client;
		packets = new HashMap[4];
		fragments = new HashMap[4];
		received = new HashMap[4];
		for (int i = 0; i < 4; i++) {
			packets[i] = new HashMap<Long, Packet>();
			fragments[i] = new HashMap<Long, Fragment>();
			received[i] = new HashMap<Long, boolean[]>();
		}
	}

	public void addPacket(Fragment fragment, Packet packet) {
		boolean dm = fragment.getHeader().getDm();
		Header header = fragment.getHeader();
		System.out.println("Packet added to packet storage seq " + header.getSeqNum() + " and frag " + header.getFragNum());
		long ID = ID(header.getSeqNum(), header.getFragNum());
		int hashMapID = header.getSource();
		HashMap<Long, Fragment> fragmentsMap = fragments[hashMapID];
		HashMap<Long, Packet> packetMap = packets[hashMapID];
		if (!fragmentsMap.containsKey(ID)) {
			packetMap.put(ID, packet);
			fragmentsMap.put(ID, fragment);
			boolean[] seen = new boolean[4];
			seen[MyProtocol.getNodeID()] = true;
			seen[header.getSource()] = true;
			// TODO: Decide whether we will use nextHop as forwarder field instead, so we can fill in 1 more space here.
			received[header.getSource()].put(ID, seen);
			if (!dm || dm && header.getDest() == MyProtocol.getNodeID()) {
				if (header.getFrag()) {
					FragHandler fragHandler;
					if (fragHandlerExists(header.getSeqNum())) {
						fragHandler = getFragHandler(header.getSeqNum());
						System.out.println(fragHandler);
						fragHandler.addFragment(fragment);
					} else {
						fragHandler = new FragHandler(fragment);
						System.out.println(fragHandler);
						addFragHandler(header.getSeqNum(), fragHandler);
						Thread fragHandlerThread = new Thread(fragHandler, "Fragmentation handler " + header.getSeqNum());
						fragHandlerThread.start();
					}
				} else if (header.getSource() != client.getNodeID()) {
					UI.printFragment(fragment);
				}
			}
			if (header.getSource() != client.getNodeID()) header.setAck();
			header.setNxtHop(client.getNodeID());
			byte[] headerBytes = header.toByteArray();
			byte[] dataBytes = packet.getData().array();
			byte[] newDataBytes = new byte[dataBytes.length - Header.HEADER_LENGTH];
			System.arraycopy(dataBytes, 3, newDataBytes, 0, header.getDataLen());
			ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + dataBytes.length).put(headerBytes).put(dataBytes);
			Packet newPacket = new Packet(DATA, byteBuffer);
			sendMessage(newPacket);
		} else {
			HashMap map = received[header.getSource()];
			boolean[] bools = (boolean[]) map.get(ID(header.getSeqNum(), header.getFragNum()));
			bools[header.getNxtHop()] = true;
			resendPacket(header.getSource(), header.getSeqNum(), header.getFragNum());
		}


		PacketRetransmitter remover = new PacketRetransmitter(header.getSource(), header.getSeqNum(), header.getFragNum(), this);
		Thread packetRemoveThread = new Thread(remover, "PacketRemover " + header.getSource() + " " +
				header.getSeqNum() + " " + header.getFragNum());
		packetRemoveThread.start();
	}

	public void resendPacket(int nodeID, int seqNum, int fragNum) {
		Fragment fragment = (Fragment) fragments[nodeID].get(ID(seqNum, fragNum));
		Packet packet = (Packet) packets[nodeID].get(ID(seqNum, fragNum));
		byte[] headerBytes = fragment.getHeader().toByteArray();
		byte[] dataBytes = packet.getData().array();
		byte[] newDataBytes = new byte[dataBytes.length - Header.HEADER_LENGTH];
		System.arraycopy(dataBytes, 3, newDataBytes, 0, fragment.getHeader().getDataLen());
		ByteBuffer byteBuffer = ByteBuffer.allocate(headerBytes.length + dataBytes.length).put(headerBytes).put(dataBytes);
		Packet newPacket = new Packet(DATA, byteBuffer);
		sendMessage(newPacket);
	}

	public void removePacket(int nodeID, int seqNum, int fragNum) {
		String sequence = padString(Integer.toBinaryString(seqNum), 5);
		String frag = padString(Integer.toBinaryString(fragNum), 5);
		long ID = Long.valueOf(sequence + frag, 2);
		packets[nodeID].remove(ID);
	}

	public void removePacket(int nodeID, int seqNum) {
		removePacket(nodeID, seqNum, 0);
	}

	public boolean hasPacket(int nodeID, int seqNum, int fragNum) {
		return fragments[nodeID].containsKey(ID(seqNum, fragNum));
	}

	public boolean hasReceived(int receiverID, int nodeID, int seqNum, int fragNum) {
		HashMap<Long, boolean[]> receivers = received[nodeID];
		boolean[] receivedByID = receivers.get(ID(seqNum, fragNum));
		return receivedByID[receiverID];
	}

	public long ID(int seqNum, int fragNum) {
		String sequence = padString(Integer.toBinaryString(seqNum), 5);
		String frag = padString(Integer.toBinaryString(fragNum), 5);
		long ID = Long.valueOf(sequence + frag, 2);
		return ID;
	}
}
















