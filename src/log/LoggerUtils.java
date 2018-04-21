package log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import entities.PeerObject;

public class LoggerUtils 
{

    private static final String logPropFile = "/logger.properties";
    private static final LoggerUtils log = new LoggerUtils(Logger.getLogger("CNT5106C"));
    
    static 
    {
        InputStream input = null;
        try
        {
            input = LoggerUtils.class.getResourceAsStream(logPropFile);
            LogManager.getLogManager().readConfiguration(input);
        }
        catch (IOException e) 
        {
            System.err.println(LoggerUtils.stackTraceToString(e));
            System.exit(1);
        }
        finally 
        {
            if (input != null) 
            {
                try 
                {
                    input.close();
                } 
                catch (IOException ex) {}
            }
        }
    }

    private final Logger logger;

    /*
     * Constructor of the LoggerUtils
     */
    private LoggerUtils(Logger logger) 
    {
        this.logger = logger;
    }

    /*
     * Configure the log for the peer
     */
    public static void configure(int pId)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException 
    {
        Properties properties = new Properties();
        properties.load(LoggerUtils.class.getResourceAsStream(logPropFile));
        Handler handler = new FileHandler ("log_peer_" + pId + ".log");
        Formatter formatter = (Formatter) Class.forName(properties.getProperty("java.util.logging.FileHandler.formatter")).newInstance();
        handler.setFormatter(formatter);
        handler.setLevel(Level.parse(properties.getProperty("java.util.logging.FileHandler.level")));
        log.logger.addHandler(handler);
    }

    /*
     * Return the logger
     */
    public static LoggerUtils getLogger() 
    {
        return log;
    }

    /*
     * Returns the list of peerIds as a String
     */
    public static String returnPeerIdString (List<Integer> pIds) 
    {
        StringBuilder sb = new StringBuilder ("");
        boolean isFirst = true;
        for(Integer peerId : pIds) 
        {
            if (isFirst) 
            {
                isFirst = false;
            }
            else 
            {
                sb.append(", ");
            }
            sb.append(peerId.intValue());
        }
        
        return sb.toString();
    }

    /*
     * Return the list of PeerIds as a string from String Object
     */
    public static String returnPeerIdStringFromObject(List<PeerObject> peers) 
    {
        StringBuilder sb = new StringBuilder ("");
        boolean isFirst = true;
        for(PeerObject peer : peers) 
        {
            if(isFirst) 
            {
                isFirst = false;
            }
            else 
            {
                sb.append(", ");
            }
            
            sb.append(peer.getId());
        }
        return sb.toString();
    }

    /*
     * configuration log
     */
    public synchronized void conf(String msg) 
    {
        logger.log(Level.CONFIG, msg);
    }

    /*
     * Debug log
     */
    public synchronized void debug(String msg) 
    {
        logger.log(Level.FINE, msg);
    }

    /*
     * info log
     */
    public synchronized void info(String msg) 
    {
        logger.log (Level.INFO, msg);
    }

    /*
     * severe log
     */
    public synchronized void severe(String msg) 
    {
        logger.log(Level.SEVERE, msg);
    }

    /*
     * warning log
     */
    public synchronized void warning(String msg) 
    {
        logger.log(Level.WARNING, msg);
    }

    /*
     * severe log
     */
    public synchronized void severe (Throwable e) 
    {
        logger.log(Level.SEVERE, stackTraceToString (e));
    }

    /*
     * warning log
     */
    public synchronized void warning (Throwable e) {
        logger.log(Level.WARNING, stackTraceToString (e));
    }

    private static String stackTraceToString(Throwable t) 
    {
        final Writer sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
