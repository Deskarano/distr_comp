package peer.server;

import peer.AbstractPeer;

import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractServerPeer extends AbstractPeer
{
    private ServerSocket serverListener;

    public AbstractServerPeer(String type)
    {
        super(type + "_server");
    }

    void listenOnPort(int port)
    {
        try
        {
            if (serverListener == null)
            {
                serverListener = new ServerSocket(port);

            }
            else if(serverListener.getLocalPort() != port)
            {
                serverListener.close();
                serverListener = new ServerSocket(port);
            }
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
            return serverListener.accept();
        }
        catch(Exception e)
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

    public abstract void run();
}
