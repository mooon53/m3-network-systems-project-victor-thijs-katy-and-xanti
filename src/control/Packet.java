package control;

import java.nio.ByteBuffer;

/**
 * Packet object which describes the type and the data.
 */
public class Packet {
    private PacketType type;
    private ByteBuffer data;


    /**
     * Packet constructor given only a type.
     *
     * @param type type of the packet
     */
    public Packet(PacketType type) {
        this.type = type;
    }

    /**
     * Packet constructor given the type and data of the packet.
     *
     * @param type type of the packet
     * @param data data to be contained in the packet
     */
    public Packet(PacketType type, ByteBuffer data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Gets the type of the packet.
     *
     * @return type of the packet
     */
    public PacketType getType() {
        return type;
    }

    /**
     * Gets the data from the packet.
     *
     * @return data from the packet
     */
    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public void setData(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length).put(data);
        setData(bb);
    }
}