import control.Client;

public class Test {
	private static int source = 1;

	private static int nodeID = 0;

	private static int currentSeqNum = 27;

	static byte[] message = new byte[]{(byte) 210, 107,(byte) 243};

	public static void main(String[] args) {
		String firstByte = Client.bytesToString(message[0]);
		System.out.println(Integer.valueOf(firstByte.substring(0,2),2));
		System.out.println(Integer.valueOf(firstByte.substring(2,4),2));
		System.out.println(Integer.parseInt(firstByte.substring(4,5))==1);
		System.out.println(Integer.parseInt(firstByte.substring(5,6))==1);
		System.out.println(Integer.parseInt(firstByte.substring(6,7))==1);
		System.out.println(Integer.parseInt(firstByte.substring(7,8))==1);
	}
}