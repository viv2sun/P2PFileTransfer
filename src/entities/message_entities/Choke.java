package entities.message_entities;

public class Choke extends MessageTemplate
{

	/*
	 * Constructor of Choke
	 */
	public Choke() 
	{
		//Choke does not have any payload, hence passing it null
		super(MessageType.Choke, null);
	}
}
