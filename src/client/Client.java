package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;


public class Client {
	private SocketChannel sock;
	private Sender sender;
	private Listener listener;

	private BlockingQueue<Message> receivedQueue;
	private BlockingQueue<Message> sendingQueue;

	private int nodeID;

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
        System.out.print("DATA: ");
        for (int i = 0; i < bytesLength; i++) {
            System.out.print(bytes.get(i) + " ");
        }
        System.out.println();
    }

    public Client(String server_ip, int server_port, int frequency, BlockingQueue<Message> receivedQueue, BlockingQueue<Message> sendingQueue, int id) {
        this.receivedQueue = receivedQueue;
        this.sendingQueue = sendingQueue;
        this.nodeID = id;
        try {
            sock = SocketChannel.open();
            sock.connect(new InetSocketAddress(server_ip, server_port));
            listener = new Listener(sock, receivedQueue);
            sender = new Sender(sock, sendingQueue);
            //dont worry, be happy (c)Thijs

			sender.sendConnect(frequency);

			listener.start();
			sender.start();
		} catch (IOException e) {
			System.err.println("Failed to connect: " + e);
			System.exit(1);
		}
	}

	public boolean isConnected() {
		return sock.isConnected();
	}

	public String padString(String input, int length) {
			while (input.length() < length) {
				input = "0" + input;
			}
			return input;
		}

		public byte setBit(byte input, int pos, boolean set) {
			if (set) {
				return (byte) (input | (1 << (8 - pos)));
			} else {
				return (byte) (input & ~(1 << (8 - pos)));
			}
		}
		public byte setBit(byte input, int pos) {
			return setBit(input, pos, true);
		}

		public boolean isSet(byte input, int pos) {
			int mask = 1 << (8 - pos);
			return (input & mask) == mask;
		}

		public byte setByte(String set) {
		byte count = 0;
        for(int i = 0; i < set.length(); i++) {
            count += Character.getNumericValue(set.charAt(i)) * Math.pow(2, set.length() - (i+1));
        }
        return count;
		}

		public static String bytesToString(byte[] input, boolean addZeroes) {
        String string = "";
        for (byte b : input) {
            BitSet bitset = BitSet.valueOf(new byte[]{b});
            int start;

            if (addZeroes) {
                start = 7;
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

	public static String bytesToString(byte[] input) {
			return bytesToString(input, true);
	}

	public static String bytesToString(byte input) {
			return bytesToString(new byte[]{input}, true);
	}

	private class Sender extends Thread {
		private BlockingQueue<Message> sendingQueue;
		private SocketChannel sock;

		public Sender(SocketChannel sock, BlockingQueue<Message> sendingQueue) {
			super();
			this.sendingQueue = sendingQueue;
			this.sock = sock;
		}

		private void senderLoop() {
			while (sock.isConnected()) {
				try {
					Message msg = sendingQueue.take();
					if (msg.getType() == MessageType.DATA || msg.getType() == MessageType.DATA_SHORT) {
						ByteBuffer data = msg.getData();
						data.position(0); //reset position just to be sure
						int length = data.capacity(); //assume capacity is also what we want to send here!
						ByteBuffer toSend = ByteBuffer.allocate(length + 2);
						if (msg.getType() == MessageType.DATA) {
							toSend.put((byte) 3);
						} else { // must be DATA_SHORT due to check above
							toSend.put((byte) 6);
						}
						toSend.put((byte) length);
						toSend.put(data);
						toSend.position(0);
						// System.out.println("Sending "+Integer.toString(length)+" bytes!");
						sock.write(toSend);
					}
				} catch (IOException e) {
					System.err.println("Alles is stuk!");
				} catch (InterruptedException e) {
					System.err.println("Failed to take from sendingQueue: " + e);
				}
			}
		}

		public byte[] createHeader(int dest, boolean syn, boolean ack, boolean frag, boolean DM, int seq,
		                           int dataLen, int nxtHop, int fragNum) {
			byte[] output = new byte[3];
			String firstByte = createFirstHeaderByte(dest, syn, ack, frag, DM);
			output[0] = setByte(firstByte);
			String secondByte = createSecondHeaderByte(seq, dataLen);
			output[1] = setByte(secondByte);
			String thirdByte = createThirdHeaderByte(dataLen, nxtHop, fragNum);
			output[2] = setByte(thirdByte);
			return output;
		}

		public String createFirstHeaderByte(int dest, boolean syn, boolean ack, boolean frag, boolean DM) {
			String firstByte = "";
			boolean[] flags = {syn, ack, frag, DM};
			firstByte += padString(Integer.toBinaryString(nodeID), 2);
			firstByte += padString(Integer.toBinaryString(dest), 2);
			for (boolean flag : flags) {
				firstByte += flag ? "1" : "0";
			}
			return firstByte;
		}

		public String createSecondHeaderByte(int seq, int dataLen) {
			String output = "";
			output += padString(Integer.toBinaryString(seq), 5);
			String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
			output += dataLenString.substring(0, 3);
			return output;
		}

		public String createThirdHeaderByte(int dataLen, int nxtHop, int fragNum) {
			String output = "";
			String dataLenString = padString(Integer.toBinaryString(dataLen), 5);
			output += dataLenString.substring(3);
			output += padString(Integer.toBinaryString(nxtHop), 2);
			output += padString(Integer.toBinaryString(fragNum), 4);
			return output;
		}

		public void sendConnect(int frequency) {
			ByteBuffer buff = ByteBuffer.allocate(4);
			buff.put((byte) 9);
			buff.put((byte) ((frequency >> 16) & 0xff));
			buff.put((byte) ((frequency >> 8) & 0xff));
			buff.put((byte) (frequency & 0xff));
			buff.position(0);
			try {
				sock.write(buff);
			} catch (IOException e) {
				System.err.println("Failed to send HELLO");
			}
		}

		public void run() {
			senderLoop();
		}

	}


	private class Listener extends Thread {
		private BlockingQueue<Message> receivedQueue;
		private SocketChannel sock;

		public Listener(SocketChannel sock, BlockingQueue<Message> receivedQueue) {
			super();
			this.receivedQueue = receivedQueue;
			this.sock = sock;
		}

		private ByteBuffer messageBuffer = ByteBuffer.allocate(1024);
		private int messageLength = -1;
		private boolean messageReceiving = false;
		private boolean shortData = false;

		private void parseMessage(ByteBuffer received, int bytesReceived) {
			// printByteBuffer(received, bytesReceived);

			try {
				for (int offset = 0; offset < bytesReceived; offset++) {
					byte d = received.get(offset);

                    if (messageReceiving) {
                        if (messageLength == -1) {
                            messageLength = (int) d;
                            messageBuffer = ByteBuffer.allocate(messageLength);
                        } else {
                            messageBuffer.put(d);
                        }
                        if (messageBuffer.position() == messageLength) {
                            // Return DATA here
                            // printByteBuffer(messageBuffer, messageLength);
                            // System.out.println("pos: "+Integer.toString(messageBuffer.position()) );
                            messageBuffer.position(0);
                            ByteBuffer temp = ByteBuffer.allocate(messageLength);
                            temp.put(messageBuffer);
                            temp.rewind();
                            //TODO: put SETUP message
                            if (setup) {
                                receivedQueue.put(new Message(MessageType.SETUP, temp));
                            } else if (shortData) {
                                receivedQueue.put(new Message(MessageType.DATA_SHORT, temp));
                            } else {
                                receivedQueue.put(new Message(MessageType.DATA, temp));
                            }
                            messageReceiving = false;
                        }
                    } else {
                        switch (d) {
                            case 0x09:
                                // System.out.println("CONNECTED");
                                receivedQueue.put(new Message(MessageType.HELLO));
                                break;
                            case 0x01:
                                // System.out.println("FREE");
                                receivedQueue.put(new Message(MessageType.FREE));
                                break;
                            case 0x02:
                                // System.out.println("BUSY");
                                receivedQueue.put(new Message(MessageType.BUSY));
                                break;
                            case 0x03:
                                messageLength = -1;
                                messageReceiving = true;
                                shortData = false;
                                break;
                            case 0x04:
                                // System.out.println("SENDING");
                                receivedQueue.put(new Message(MessageType.SENDING));
                                break;
                            case 0x05:
                                // System.out.println("DONE_SENDING");
                                receivedQueue.put(new Message(MessageType.DONE_SENDING));
                                break;
                            case 0x06:
                                messageLength = -1;
                                messageReceiving = true;
                                shortData = true;
                                break;
                            case 0x08:
                                // System.out.println("END");
                                receivedQueue.put(new Message(MessageType.END));
                                break;
                            case 0x10:
                                messageLength = -1;
                                messageReceiving = true;
                                setup = true;
                                break;
                            default:
                                System.out.println();
                                break;
                        }
                    }
                }

			} catch (InterruptedException e) {
				System.err.println("Failed to put data in receivedQueue: " + e.toString());
			}
		}

		public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
			System.out.print("DATA: ");
			for (int i = 0; i < bytesLength; i++) {
				System.out.print(bytes.get(i) + " ");
			}
			System.out.println();
		}

		public void receivingLoop() {
			int bytesRead = 0;
			ByteBuffer recv = ByteBuffer.allocate(1024);
			try {
				while (sock.isConnected()) {
					bytesRead = sock.read(recv);
					if (bytesRead > 0) {
						// System.out.println("Received "+Integer.toString(bytesRead)+" bytes!");
						parseMessage(recv, bytesRead);
					} else {
						break;
					}
					recv.clear();
				}
			} catch (IOException e) {
				System.err.println("Error on socket: " + e);
			}

		}

		public void run() {
			receivingLoop();
		}

	}
}