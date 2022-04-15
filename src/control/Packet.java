package control;

import java.nio.ByteBuffer;

public class Packet {
	private PacketType type;
	private ByteBuffer data;

	public Packet(PacketType type) {
		this.type = type;
	}

	public Packet(PacketType type, ByteBuffer data) {
		this.type = type;
		this.data = data;
	}

	public PacketType getType() {
		return type;
	}

	public ByteBuffer getData() {
		return data;
	}
}