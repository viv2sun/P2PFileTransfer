package entities.message_entities;


public enum MessageType {
    Choke((byte) 0),
    Unchoke((byte) 1),
    Interested((byte) 2),
    NotInterested((byte) 3),
    Have((byte) 4),
    BitField((byte) 5),
    Request((byte) 6),
    Piece((byte) 7);

    private final byte type;
    
    MessageType(byte type) {
        this.type = type;
    }

    public byte value() {
        return this.type;
    }

    public static MessageType valueOf(byte b) {
        for (MessageType t : MessageType.values()) {
            if (t.type == b) {
                return t;
            }
        }
        return null;
    }
}
