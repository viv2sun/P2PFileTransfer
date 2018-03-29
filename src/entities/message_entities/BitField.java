package entities.message_entities;

import java.util.BitSet;

public class BitField extends MessageTemplate{
	
	public BitField(byte[] bitfield) {
        super(MessageType.BitField, bitfield);
    }

    public BitField(BitSet bitset) {
        super(MessageType.BitField, bitset.toByteArray());
    }

    public BitSet getBitSet() {
        return BitSet.valueOf(payload);
    }
}
