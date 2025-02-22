package control;

import model.*;
import model.objects.*;
import view.DebugInterface;
import view.UI;

import static utils.HelpFunc.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static model.objects.PacketType.*;

/**
 * The implementation of our protocol.
 */
public class MyProtocol {

	// The host to connect to. Set this to localhost when using the audio interface tool.
	private static final String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
	// The port to connect to. 8954 for the simulation server.
	private static final int SERVER_PORT = 8954;
	// The frequency to use.
	private static int frequency = 500 + 9 * 100;

	BlockingQueue<Packet> receivedQueue = new LinkedBlockingQueue<>();
	static BlockingQueue<Packet> sendingQueue = new LinkedBlockingQueue<>();

	private final Client client;
	private UI ui;
	private PacketStorage packetStorage;

	private static int nodeID;
	private int sequenceNumber;

	private PingListener[] pingListeners = new PingListener[4];

	public static final boolean DEBUGGING_MODE = false;

	/**
	 * Constructor of the protocol, starting a client,
	 * receiving thread, while waiting for text input.
	 *
	 * @param serverIp   IP of the server to connect to
	 * @param serverPort port of the server to connect to
	 * @param frequency  frequency on which the communication should take place
	 */
	public MyProtocol(String serverIp, int serverPort, int frequency) {
		// give the client the Queues to use
		client = new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue, nodeID);
		// start thread to handle received messages!
		new ReceiveThread(receivedQueue).start();
		packetStorage = new PacketStorage(client);

		ui = new UI();
		sequenceNumber = 0;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;

			boolean gotNodeID = false;

			// TODO: send the setup and find index using SYN
			// get everyone to pick a nodeID between (0-3)
			input = ui.getInput(br, "What number (0-3) do you want as your nodeID?");
			while (!gotNodeID) {
				try {
					int id = Integer.parseInt(input);
					if (nodeID >= 0 && nodeID <= 3) {
						gotNodeID = true;
						nodeID = id;
						client.setNodeID(nodeID);
						client.ping();
					} else {
						throw new NumberFormatException("Number must be at least 0 and at most 3");
					}
				} catch (NumberFormatException e) {
					input = ui.getInput(br, "Please fill in a number between 0 and 3");
				}
			}

			// handle sending from System.in from this thread
			while (!br.ready()) {
				// read input
				input = ui.getInput(br);
				while (input.length() > 463) {
					input = ui.getInput(br, "Please put in a message " +
							"with a maximum of 463 characters");
				}
				byte[] inputBytes = input.getBytes(); // get bytes from input
				Header standardHeader = new Header(nodeID, 0, false, false, false, false,
						sequenceNumber, 0, nodeID, 0);
				PacketEncoder packetEncoder = new PacketEncoder(inputBytes, standardHeader);
				if (DEBUGGING_MODE) System.out.println(Arrays.deepToString(packetEncoder.fragmentedMessage()));
				byte[][] fragments = packetEncoder.fragmentedMessage();
				for (byte[] packet : fragments) {
					ByteBuffer byteBuffer = ByteBuffer.allocate(packet.length).put(packet);
					byte[] headerBytes = new byte[3];
					System.arraycopy(packet, 0, headerBytes, 0, 3);
					Header tempHeader = new Header(headerBytes);
					Fragment fragment = new Fragment(tempHeader, input);
					Packet pack = new Packet(DATA, byteBuffer);
					if (DEBUGGING_MODE) System.out.println(Arrays.toString(byteBuffer.array()));
					packetStorage.addPacket(fragment, pack);
				}
				sequenceNumber++;
				sequenceNumber = sequenceNumber % 32;
			}
		} catch (IOException e) {
			System.exit(2);
		}
	}

	/**
	 * Sends a packet to the sendingQueue.
	 *
	 * @param packet    bytes that should be sent, including the header
	 * @param shortData whether or not the packet is a DATA_SHORT
	 */
	public static void sendPacket(byte[] packet, boolean shortData) {
		try {
			// make a new byte buffer in which you put the packet
			ByteBuffer toSend = ByteBuffer.allocate(packet.length);
			toSend.put(packet, 0, packet.length);
			if (DEBUGGING_MODE) {
				DebugInterface.printPacket(new Packet(DATA, toSend), "");
			}
			Packet msg;
			if (shortData) {
				msg = new Packet(DATA_SHORT, toSend);
			} else {
				msg = new Packet(DATA, toSend);
			}
			sendingQueue.put(msg);
		} catch (InterruptedException e) {
			//TODO: decide what to do
		}
	}

	/**
	 * Sends a long data packet to the sendingQueue.
	 *
	 * @param packet bytes that should be sent, including the header
	 */
	public static void sendPacket(byte[] packet) {
		sendPacket(packet, false);
	}

	/**
	 * Main method starting a new MyProtocol.
	 *
	 * @param args wanted frequency as program argument
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			frequency = Integer.parseInt(args[0]);
		}
		new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
	}

	/**
	 * Puts a message in the sendingQueue.
	 *
	 * @param packet packet which needed to be added to the sendingQueue
	 */
	public static void sendMessage(Packet packet) {
		try {
			sendingQueue.put(packet);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int getNodeID() {
		return nodeID;
	}

	private class ReceiveThread extends Thread {
		private final BlockingQueue<Packet> receivedQueue;

		/**
		 * Constructor for the receiveThread, setting the receivedQueue.
		 *
		 * @param receivedQueue receivedQueue to which this Thread's queue should be set to
		 */
		public ReceiveThread(BlockingQueue<Packet> receivedQueue) {
			super();
			this.receivedQueue = receivedQueue;
		}

		// Handle messages from the server / audio framework

		/**
		 * Runs the Thread for receiving messages.
		 */
		public void run() {
			while (client.isConnected()) {
				try {
					// take a packet from the queue and process it
					Packet m = receivedQueue.take();
					PacketType p = m.getType();
					if (DEBUGGING_MODE) {
						DebugInterface.printPacketType(p);
					}

					// switch over the data type and handle accordingly
					switch (p) {
						case BUSY:
							break;
						case FREE:
							break;
						case DATA:
							// for a data packet, make a packetDecoder
							PacketDecoder packetDecoder = new PacketDecoder(m.getData().array(),
									m, packetStorage);
							// for the decoder, make a messageHandler and start it as a thread
							Thread messageHandler = new Thread(packetDecoder, "message handler");
							messageHandler.start();
							break;
						case DATA_SHORT:
							byte b = m.getData().array()[0];
							String s = byteToString(b).substring(0, 2);
							b = stringToByte(s);
							client.setInRange(b);
							if (DEBUGGING_MODE) System.out.println(Arrays.toString(client.getInRange()));
							if (pingListeners[b] != null) { // If the PingListener for the node exists
								pingListeners[b].disable(); // Disable it (so it doesn't set the node to unreachable)
							}
							PingListener pingListener = new PingListener(b, client); // Create new PingListener
							Thread pingListenerThread = new Thread(pingListener, "Ping listener for node " + b); // Create new thread to run it in
							pingListeners[b] = pingListener; // Add PingListener to the list of PingListener with the index being the node's id
							pingListenerThread.start(); // Start PingListener thread
							break;
						case DONE_SENDING:
							break;
						case HELLO:
							break;
						case SENDING:
							break;
						case END:
							System.exit(0);
							break;
						case SETUP:
							if (DEBUGGING_MODE) {
								DebugInterface.printSetup(nodeID);
							}
							break;
						default:
							System.out.println();
							break;
					}
				} catch (InterruptedException e) {
					System.err.println("Failed to take from queue: " + e);
				}
			}
		}
	}
}