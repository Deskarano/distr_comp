import java.io.*;
import java.net.*;
import java.lang.Exception;
import java.util.ArrayList;


public class ServiceRequestThread extends Thread
{
    private Object[][] routingTable;
    private Socket peerSocket;

    // Constructor
    ServiceRequestThread(Object[][] routingTable, Socket peerSocket) throws IOException
    {
        this.routingTable = routingTable;
        this.peerSocket = peerSocket;
    }

    // Run method (will run for each machine that connects to the ServerRouter)
    public void run()
    {

    }
}