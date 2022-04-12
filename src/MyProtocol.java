import client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is just some example code to show you how to interact
 * with the server using the provided 'Client' class and two queues.
 * Feel free to modify this code in any way you like!
 */

public class MyProtocol {

	// The host to connect to. Set this to localhost when using the audio interface tool.
	private static final String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
	// The port to connect to. 8954 for the simulation server.
	private static final int SERVER_PORT = 8954;
	// The frequency to use.
	private static int frequency = 500 + 9 * 100;//TODO: Set this to your group frequency!

	private final Client client;
	private int nodeID;

	public MyProtocol(String server_ip, int server_port, int frequency) {
		BlockingQueue<Message> receivedQueue = new LinkedBlockingQueue<>();
		BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<>();

		client = new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue); // Give the client the Queues to use

		new receiveThread(receivedQueue).start(); // Start thread to handle received messages!

		// handle sending from stdin from this thread.
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
			while (!br.ready()) {
				input = br.readLine(); // read input
				System.out.println(input);
				byte[] inputBytes = input.getBytes(); // get bytes from input
				for (byte i : inputBytes) {
					System.out.println(i);
				}

				byte[] packet = new byte[32];

				// source node, ack-flag & data-length
				packet[0] = stringToByte(
						bytesToString(new byte[]{(byte) nodeID}, false)
								+ "0" //TODO: ACK-flag algorithm
								+ bytesToString(new byte[]{(byte) inputBytes.length}, false)
				);

				// sequence number; TODO: sequence number

				// syn, next-hop, fragmentation-flag & fragmentation-number
				packet[2] = stringToByte(
						"0" //TODO: syn
								+ bytesToString(new byte[]{(byte) nodeID}, false) //TODO: next-hop
								+ "0" //TODO: fragmentation-flag
								+ "0000" //TODO: fragmentation-number
				);

				// error detection (ignoring the second and third byte)
				packet[3] = xorCheck(concatArray(new byte[]{packet[0]}, concatArray(new byte[]{packet[1]}, inputBytes)));

				// data
				for (int i = 0; i < inputBytes.length; i++) {
					packet[4 + i] = inputBytes[i];
				}

				ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length); // make a new byte buffer with the length of the input string
				toSend.put(inputBytes, 0, inputBytes.length); // copy the input string into the byte buffer.
				Message msg;
				if ((input.length()) > 2) {
					msg = new Message(MessageType.DATA, toSend);
				} else {
					msg = new Message(MessageType.DATA_SHORT, toSend);
				}
				sendingQueue.put(msg);
			}
		} catch (InterruptedException | IOException e) {
			System.exit(2);
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			frequency = Integer.parseInt(args[0]);
		}
		new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
	}

	public byte stringToByte(String string) {
        byte count = 0;
        for(int i = 0; i < string.length(); i++) {
            count += Character.getNumericValue(string.charAt(i)) * Math.pow(2, string.length() - (i+1));
        }
        return count;
    }

    public String bytesToString(byte[] input, boolean addZeroes) {
        String string = "";
        for (byte b : input) {
            BitSet bitset = BitSet.valueOf(new byte[]{b});
            int start;

            if (addZeroes) {
                start = 8;
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

    public byte xorCheck(byte[] bytes) {
        if (bytes.length >= 2) {
            byte xoredByte = (byte) (bytes[0] ^ bytes[1]);
            for (int i = 2; i < bytes.length; i++) {
                xoredByte = (byte) (xoredByte ^ bytes[i]);
            }
            return xoredByte;
        } else if (bytes.length == 1) {
            return bytes[0];
        } else {
            return 0;
        }
    }

    public byte[] concatArray(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        for (int i = 0; i < array1.length; i++) {
            result[i] = array1[i];
        }

        for (int i = 0; i < array2.length; i++) {
            result[i+ array1.length] = array2[i];
        }

        return result;
    }

	private class receiveThread extends Thread {
		private final BlockingQueue<Message> receivedQueue;

		public receiveThread(BlockingQueue<Message> receivedQueue) {
			super();
			this.receivedQueue = receivedQueue;
		}

		public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
			for (int i = 0; i < bytesLength; i++) {
				System.out.print(bytes.get(i) + " ");
			}
			System.out.println();
		}

		// Handle messages from the server / audio framework
		public void run() {
			while (client.isConnected()) {
				try {
					Message m = receivedQueue.take();
					switch (m.getType()) {
						case BUSY:
							System.out.println("BUSY");
							break;
						case FREE:
							System.out.println("FREE");
							break;
						case DATA:
							System.out.print("DATA: ");
							printByteBuffer(m.getData(), m.getData().capacity()); //Just print the data
							try {
								for (int i = 0; i < m.getData().capacity(); i++) {
									System.out.print(Character.toString(m.getData().get(i)));
									/*BitSet bitset = BitSet.valueOf(m.getData());
									for (int n = 0; n<bitset.length(); n++) {
										int bit = bitset.get(n) ? 1:0;
										System.out.print(bit);
									}*/
								}
							} catch (IllegalArgumentException e) {
								System.out.println(";");
							}
							break;
						case DATA_SHORT:
							System.out.print("DATA_SHORT: ");
							printByteBuffer(m.getData(), m.getData().capacity()); //Just print the data
							try {
								for (int i = 0; i < m.getData().capacity(); i++) {
									System.out.print(Character.toString(m.getData().get(i)));
									/*BitSet bitset = BitSet.valueOf(m.getData());
									for (int n = 0; n<bitset.length(); n++) {
										int bit = bitset.get(n) ? 1:0;
										System.out.print(bit);
									}*/
								}
								System.out.println(";");
							} catch (IllegalArgumentException e) {
								System.out.println(";");
							}
							break;
						case DONE_SENDING:
							System.out.println("DONE_SENDING");
							break;
						case HELLO:
							System.out.println("HELLO");
							break;
						case SENDING:
							System.out.println("SENDING");
							break;
						case END:
							System.out.println("END");
							System.exit(0);
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