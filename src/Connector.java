import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import manager.*;
import entities.message_entities.HandShakeMessageTemplate;
import entities.message_entities.MessageTemplate;
import entities.message_entities.Request;
import entities.message_entities.TimerEntity;
import io.ProtocolInputStream;
import io.ProtocolObjectOutputStream;
import log.LoggerMain;
import log.LoggerUtils;


public class Connector implements Runnable {
	
	private static final int PEER_NOT_SET = -1;
	
	private final int locPeerId;
    private final Socket socket;
    private final ProtocolObjectOutputStream out;
    private final FileManager fm;
    private final PeerManager pm;
    private final boolean isConnected;
    private final int expRemPId;
    private final AtomicInteger remPeerId;
    private final BlockingQueue<MessageTemplate> q = new LinkedBlockingQueue<>();
    
	public Connector(int id, Socket s, FileManager fm, PeerManager pm) throws IOException 
	{
	     this(id, false, -1, s, fm, pm);
	}
	
	public Connector(int id, boolean isConnected, int expectedPeerId,Socket socket, FileManager fm, PeerManager pm) 
			throws IOException 
	{
		this.socket = socket;
		this.locPeerId = id;
		this.isConnected = isConnected;
		this.expRemPId = expectedPeerId;
		this.fm = fm;
		this.pm = pm;
		this.out = new ProtocolObjectOutputStream(socket.getOutputStream());
		this.remPeerId = new AtomicInteger(PEER_NOT_SET);
	}
	
	public int getPeerId() 
	{
		return remPeerId.get();
	}

	 @Override
    public void run() 
	 {
        new Thread() 
        {
            private boolean isChoked = true;

            @Override
            public void run() 
            {
                Thread.currentThread().setName(getClass().getName() + "-" + remPeerId + "-sending thread");
                while(true) 
                {
                    try 
                    {
                        final MessageTemplate message = q.take();
                        
                        if(message == null) 
                        {
                            continue;
                        }
                        
                        if(remPeerId.get() != PEER_NOT_SET) 
                        {
                            switch (message.type) 
                            {
                                case Choke: 
                                {
                                    if (!isChoked) 
                                    {
                                        isChoked = true;
                                        sendInternal(message);
                                    }
                                    break;
                                }

                                case Unchoke: 
                                {
                                    if (isChoked) 
                                    {
                                        isChoked = false;
                                        sendInternal(message);
                                    }
                                    
                                    break;
                                }

                                default:
                                    sendInternal(message);
                            }
                        } else 
                        {
                            LoggerUtils.getLogger().debug("cannot send message of type "
                                    + message.type + " because the remote peer has not handshaked yet.");
                        }
                    } 
                    catch(IOException ex) 
                    {
                        LoggerUtils.getLogger().warning(ex);
                    } 
                    catch (InterruptedException ex) 
                    {	}
                }
            }
        }.start();

        try 
        {
            final ProtocolInputStream in = new ProtocolInputStream(socket.getInputStream());

            out.writeObject(new HandShakeMessageTemplate(locPeerId));
            
            HandShakeMessageTemplate rcvdHandshake = (HandShakeMessageTemplate)in.readObject();
            
            remPeerId.set(rcvdHandshake.returnPeerId());
            
            Thread.currentThread().setName(getClass().getName() + "-" + remPeerId.get());
            final LoggerMain LoggerMain = new LoggerMain(locPeerId);
            final MessageHandler msgHandler = new MessageHandler(remPeerId.get(), fm, pm, LoggerMain);
            if (isConnected && (remPeerId.get() != expRemPId)) 
            {
                throw new Exception("Remote peer id " + remPeerId + " does not match with the expected id: " + expRemPId);
            }

            // Handshake successful
            LoggerMain.peerConnection(remPeerId.get(), isConnected);

            sendInternal(msgHandler.handle(rcvdHandshake));
            while(true) 
            {
                try 
                {
                    sendInternal(msgHandler.handle((MessageTemplate)in.readObject()));
                } 
                catch (Exception ex) 
                {
                    LoggerUtils.getLogger().warning(ex);
                    break;
                }
            }
        } 
        catch (Exception ex) 
        {
            LoggerUtils.getLogger().warning(ex);
        } 
        finally 
        {
            try 
            {
                socket.close();
            } 
            catch (Exception e) 
            {	}
        }
        LoggerUtils.getLogger().warning(Thread.currentThread().getName()
                + " terminating, messages will no longer be accepted.");
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof Connector) 
        {
            return ((Connector) obj).remPeerId == remPeerId;
        }
        
        return false;
    }

    @Override
    public int hashCode() 
    {
        int hash = 7;
        hash = 41 * hash + locPeerId;
        return hash;
    }

    public void send(final MessageTemplate msg) 
    {
        q.add(msg);
    }

    private synchronized void sendInternal(MessageTemplate msg) throws IOException 
    {
        if (msg != null) 
        {
            out.writeObject(msg);
            
            switch(msg.getType()) 
            {
                case Request: 
                {
                    new java.util.Timer().schedule(
                            new TimerEntity((Request) msg, fm, out, msg, remPeerId.get()),
                            (long)pm.getIntervalForUnchoking() * 2
                    );
                }
            }
        }
    }
}
