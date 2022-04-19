package control;

import model.FragHandler;
import view.UI;

import java.util.HashMap;

import static control.Client.*;
import static utils.HelpFunc.*;

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

	public PacketStorage() {
		packets = new HashMap[3];
		fragments = new HashMap[3];
		received = new HashMap[3];
		for (int i = 0; i < 3; i++) {
			packets[i] = new HashMap<Long, Packet>();
			fragments[i] = new HashMap<Long, Fragment>();
			received[i] = new HashMap<Long, boolean[]>();
		}
	}

	public void addPacket(Fragment fragment, Packet packet, boolean dm) {
		Header header = fragment.getHeader();
		System.out.println("Packet added to packet storage seq " + header.getSeqNum() + " and frag " + header.getFragNum());
		long ID = ID(header.getSeqNum(), header.getFragNum());
		int hashMapID = hashMapID(header.getSource());
		HashMap<Long, Fragment> fragmentsMap = fragments[hashMapID];
		HashMap<Long, Packet> packetMap = packets[hashMapID];
		if (!fragmentsMap.containsKey(ID)) {
			packetMap.put(ID, packet);
			fragmentsMap.put(ID, fragment);
			boolean[] seen = new boolean[4];
			seen[MyProtocol.getNodeID()] = true;
			seen[header.getSource()] = true;
			// TODO: Decide whether we will use nextHop as forwarder field instead, so we can fill in 1 more space here.
			received[hashMapID(header.getSource())].put(ID, seen);
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
				} else {
					UI.printFragment(fragment);
				}
			}
		} else {
//			if (!hasReceived(forwarder, fragment.getSourceID(), fragment.getSeqNum(), fragment.getFragID())) {
//				received[fragment.getSourceID()].get(ID)[forwarder] = true;
//			}
		}

		PacketRetransmitter remover = new PacketRetransmitter(header.getSource(), header.getSeqNum(), header.getFragNum(), this);
		Thread packetRemoveThread = new Thread(remover, "PacketRemover " + header.getSource() + " " +
				header.getSeqNum() + " " + header.getFragNum());
		packetRemoveThread.start();
	}

	public void addPacket(Fragment fragment, Packet packet) {
		addPacket(fragment, packet, false);
	}

	public void removePacket(int nodeID, int seqNum, int fragNum) {
		String sequence = padString(Integer.toBinaryString(seqNum), 5);
		String frag = padString(Integer.toBinaryString(fragNum), 5);
		long ID = Long.valueOf(sequence + frag, 2);
		packets[hashMapID(nodeID)].remove(ID);
	}

	public void removePacket(int nodeID, int seqNum) {
		removePacket(nodeID, seqNum, 0);
	}

	public boolean hasPacket(int nodeID, int seqNum, int fragNum) {
		return fragments[hashMapID(nodeID)].containsKey(ID(seqNum, fragNum));
	}

	public boolean hasReceived(int receiverID, int nodeID, int seqNum, int fragNum) {
		HashMap<Long, boolean[]> receivers = received[hashMapID(nodeID)];
		boolean[] receivedByID = receivers.get(ID(seqNum, fragNum));
		return receivedByID[receiverID];
	}

	public int hashMapID(int nodeID) {
		if (nodeID > MyProtocol.getNodeID()) return nodeID - 1;
		return nodeID;
	}

	public long ID(int seqNum, int fragNum) {
		String sequence = padString(Integer.toBinaryString(seqNum), 5);
		String frag = padString(Integer.toBinaryString(fragNum), 5);
		long ID = Long.valueOf(sequence + frag, 2);
		return ID;
	}
}
















