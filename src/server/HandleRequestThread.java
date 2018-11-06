package server;

import java.io.*;
import java.net.*;

import protocol.Protocol;

public class HandleRequestThread extends Thread
{
    private static final String HEADER = "(HandleRequestThread)";

    public static String otherServerRouterIP;
    public static int otherServerRouterPort;

    private Object[][] routingTable;
    private Socket peerSocket;

    // Constructor
    HandleRequestThread(Object[][] routingTable, Socket peerSocket)
    {
        this.routingTable = routingTable;
        this.peerSocket = peerSocket;
    }

    private String requestFromSame(String type)
    {
        for (int i = 0; i < routingTable.length; i++)
        {
            if (routingTable[i][1].equals(type))
            {
                return (String) routingTable[i][0];
            }
        }

        return Protocol.RESPONE_NONE;
    }

    private String requestFromOther(String type)
    {
        String result = Protocol.RESPONE_NONE;
        try
        {
            Socket otherServerRouter = new Socket(otherServerRouterIP, otherServerRouterPort);

            PrintWriter serverRouterWriter = new PrintWriter(otherServerRouter.getOutputStream(), true);
            BufferedReader serverRouterReader = new BufferedReader(new InputStreamReader(otherServerRouter.getInputStream()));

            serverRouterWriter.println(Protocol.REQUEST_COMMAND + " " + type + " " + Protocol.REQUEST_FROM_SAME);
            result = serverRouterReader.readLine();

            serverRouterWriter.close();
            serverRouterReader.close();
            otherServerRouter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }

        return result;
    }

    // Run method (will run for each machine that connects to the ServerRouter)
    public void run()
    {
        String peerIP = peerSocket.getInetAddress().getHostAddress();
        boolean peerFound = false;

        for (int i = 0; i < routingTable.length; i++)
        {
            if (peerIP.equals(routingTable[i][0]))
            {
                peerFound = true;
                routingTable[i][2] = peerSocket;
                break;
            }
        }

        if (peerFound)
        {
            try
            {
                PrintWriter peerWriter = new PrintWriter(peerSocket.getOutputStream(), true);
                BufferedReader peerReader = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));

                while (!peerSocket.isClosed())
                {
                    String input = peerReader.readLine();
                    String[] command = input.split(" ");

                    switch (command[0])
                    {
                        case Protocol.REQUEST_COMMAND:
                            String type = command[1];
                            String from = command[2];

                            switch (from)
                            {
                                case Protocol.REQUEST_FROM_SAME:
                                    peerWriter.println(requestFromSame(type));
                                    break;

                                case Protocol.REQUEST_FROM_OTHER:
                                    peerWriter.println(requestFromOther(type));
                                    break;

                                case Protocol.REQUEST_FROM_NEAR:
                                    String nearResult = requestFromSame(type);
                                    if (nearResult.equals(Protocol.RESPONE_NONE))
                                    {
                                        nearResult = requestFromOther(type);
                                    }

                                    peerWriter.println(nearResult);
                                    break;

                                case Protocol.REQUEST_FROM_FAR:
                                    String farResult = requestFromOther(type);
                                    if (farResult.equals(Protocol.RESPONE_NONE))
                                    {
                                        farResult = requestFromSame(type);
                                    }

                                    peerWriter.println(farResult);
                                    break;

                                default:
                                    peerWriter.println(Protocol.RESPONSE_INVALID);
                                    break;
                            }
                    }
                }

                peerWriter.close();
                peerReader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
        else
        {
            try
            {
                System.out.println(HEADER + ": unregistered peer, disconnecting");

                PrintWriter peerWriter = new PrintWriter(peerSocket.getOutputStream(), true);
                peerWriter.println("denied");

                peerWriter.close();
                peerSocket.close();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
    }
}