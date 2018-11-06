package peer.client;

import peer.AbstractPeer;
import protocol.Protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class AbstractClientPeer extends AbstractPeer
{
    public AbstractClientPeer(String type)
    {
        super(type + "_client");
    }

    private String requestPeer(String type, String from)
    {
        try
        {
            Socket serverRouter = new Socket(serverRouterIP, serverRouterPort);

            PrintWriter serverRouterWriter = new PrintWriter(serverRouter.getOutputStream());
            BufferedReader serverRouterReader = new BufferedReader(new InputStreamReader(serverRouter.getInputStream()));

            serverRouterWriter.println(Protocol.HEADER_REQUEST + type + " " + from);
            String response = serverRouterReader.readLine();

            serverRouterWriter.close();
            serverRouterReader.close();
            serverRouter.close();

            return response;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            return "";
        }
    }

    private Socket connectToPeer(String IP, int port)
    {
        try
        {
            return new Socket(IP, port);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            return null;
        }
    }

    Socket connectToServer(String type, String from)
    {
        String response = requestPeer(type + "_server", from);
        if (response.equals(Protocol.RESPONE_NONE))
        {
            return null;
        }
        else
        {
            String[] splitIP = response.split(":");
            return connectToPeer(splitIP[0], Integer.parseInt(splitIP[1]));
        }
    }

    public abstract void run();
}
