package entities.message_entities;

public class NotInterested extends MessageTemplate
{
	/*
	 * Constructor of the Not Interested Message
	 * Payload is passed as null, since there is no payload for Not Interested message
	 */
	public NotInterested() 
	{
        super(MessageType.NotInterested, null);
    }
}
