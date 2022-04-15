import control.Client;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class Test {
	public static void main(String[] args) {
		byte b = 0x03;
		String test = new String(new byte[]{b}, StandardCharsets.UTF_8);
		System.out.println(test);
		byte test2 = test.getBytes(StandardCharsets.UTF_8)[0];
		boolean bool = test.endsWith("");
		System.out.println(bool);
	}
}