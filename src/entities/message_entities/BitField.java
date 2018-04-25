package entities.message_entities;

import java.util.BitSet;

public class BitField extends MessageTemplate
{
	
	/*
	 * Constructor
	 */
	public BitField(byte[] bitfield) 
	{
    super(MessageType.BitField, bitfield);
  }

	/*
	 * Constructor
	 */
  public BitField(BitSet bitset) 
  {
    super(MessageType.BitField, bitset.toByteArray());
  }

  /*
   * Getting the payload(byte array) of the bitfield as the BitSet
   */
  public BitSet getBitSet() 
  {
    return BitSet.valueOf(payload);
  }
}
