package peer.server;

import peer.AbstractPeer;

import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractServerPeer extends AbstractPeer
{
    private static final String HEADER = "(AbstractServerPeer)";
    private ServerSocket serverListener;

    public AbstractServerPeer(String type)
    {
        super(type + "_server");
    }

    void listenOnPort(int port)
    {
        try
        {
            System.out.println(HEADER + ": starting new serversocket on port " + port);
            serverListener = new ServerSocket(port);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

    Socket getClient()
    {
        try
        {
            System.out.println(HEADER + ": listening for new client...");
            Socket client = serverListener.accept();
            System.out.println(HEADER + ": found new client at " + client.getInetAddress().getHostAddress());

            return client;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    Socket getClient(int timeout)
    {
        try
        {
            serverListener.setSoTimeout(timeout);
            return serverListener.accept();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            return null;
        }
    }

    public abstract void run(int port);
}
