package peer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import protocol.Protocol;

public abstract class AbstractPeer
{
    private static final String HEADER = "(AbstractPeer)";
    protected String type;

    protected String serverRouterIP;
    protected  int serverRouterPort;

    public AbstractPeer(String type)
    {
        this.type = type;
    }

    public boolean registerToServerRouter(String routerIP, int routerPort, int listenPort)
    {
        try
        {
            Socket serverRouter = new Socket(routerIP, routerPort);
            System.out.println(HEADER + ": connected to ServerRouter at " + routerIP + ":" + routerPort);

            //init readers and writers
            PrintWriter serverRouterWriter = new PrintWriter(serverRouter.getOutputStream(), true);
            BufferedReader serverRouterReader = new BufferedReader(new InputStreamReader(serverRouter.getInputStream()));

            //send initial message: peer type
            String initMessage = Protocol.HEADER_TYPE + this.type;
            if(listenPort != -1)
            {
                initMessage += " " + listenPort;
            }

            System.out.println(HEADER + ": sending initial message to ServerRouter: " + initMessage);

            serverRouterWriter.println(initMessage);

            //wait for ServerRouter to be ready
            String response = serverRouterReader.readLine();

            System.out.println(HEADER + ": received response " + response);
            String[] responseSplit = response.split(" ");

            if (responseSplit[0].equals(Protocol.HEADER_START))
            {
                serverRouterIP = routerIP;
                serverRouterPort = Integer.parseInt(responseSplit[1]);
                System.out.println(HEADER + ": serverRouterIP = " + serverRouterIP + ", serverRouterPort = " + serverRouterPort);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            return false;
        }
    }
}
