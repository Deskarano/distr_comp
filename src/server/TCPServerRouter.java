package server;

import java.net.*;
import java.io.*;
import java.util.Scanner;

import protocol.Protocol;

public class TCPServerRouter
{
    private final static String HEADER = "(ServerRouter)";

    private final static String COMMAND_CONNECT_SR = "connect_sr";
    private final static String COMMAND_LISTEN_SR = "listen_sr";
    private final static String COMMAND_ACCEPT_PEERS = "accept_peers";
    private final static String COMMAND_START = "start";
    private final static String COMMAND_HELP = "help";

    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);

        Socket otherServerRouter = null;

        String otherServerRouterIP = null;
        int otherServerRouterPort = 0;

        /**
         * Routing table format:
         *      peerRoutingTable[i][0]: peer IP address
         *      peerRoutingTable[i][1]: peer type
         *      peerRoutingTable[i][2]: socket to peer
         */
        Object[][] peerRoutingTable = null;

        while (true)
        {
            //get next command
            System.out.print(HEADER + " >>> ");
            String read = input.nextLine();
            String[] command = read.split(" ");

            try
            {
                switch (command[0])
                {
                    case COMMAND_CONNECT_SR: // args: [ip address] [port]
                        System.out.println(HEADER + ": connecting to other SR at " + command[1] + ":" + command[2]);
                        try
                        {
                            //connect to other ServerRouter and save IP/port information
                            otherServerRouter = new Socket(command[1], Integer.parseInt(command[2]));

                            otherServerRouterIP = command[1];
                            otherServerRouterPort = Integer.parseInt(command[2]);

                            System.out.println(HEADER + ": connected to " + otherServerRouterIP + ":" + otherServerRouterPort);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;
                    case COMMAND_LISTEN_SR: // args: [port] [timeout (seconds)]
                        try
                        {
                            System.out.println(HEADER + ": listening for other SR on port " + command[1]);

                            int timeout = Integer.parseInt(command[2]) * 1000;

                            //listen for an incoming connection
                            ServerSocket serverRouterListener = new ServerSocket(Integer.parseInt(command[1]));
                            serverRouterListener.setSoTimeout(timeout);
                            otherServerRouter = serverRouterListener.accept();

                            serverRouterListener.close();

                            //save IP/port information
                            otherServerRouterIP = otherServerRouter.getInetAddress().getHostAddress();
                            otherServerRouterPort = Integer.parseInt(command[1]);

                            System.out.println(HEADER + ": connected to " + otherServerRouterIP + ":" + otherServerRouterPort);
                        }
                        catch (Exception e)
                        {
                            System.out.println(HEADER + ": timed out");
                            e.printStackTrace(System.out);
                        }

                        break;
                    case COMMAND_ACCEPT_PEERS: // args: [num_peers] [port]
                        int numPeers = Integer.parseInt(command[1]);
                        peerRoutingTable = new Object[numPeers][3];

                        ServerSocket peerListener = null;
                        int listenPort = Integer.parseInt(command[2]);

                        try
                        {
                            peerListener = new ServerSocket(listenPort);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.out);
                        }

                        if (peerListener != null)
                        {
                            for (int i = 0; i < numPeers; i++)
                            {
                                try
                                {
                                    System.out.println(HEADER + ": listening for peer " + i);
                                    Socket peerSocket = peerListener.accept();

                                    BufferedReader peerReader = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
                                    String response = peerReader.readLine();
                                    String[] splitResponse = response.split(" ");

                                    String peerIP = peerSocket.getInetAddress().getHostAddress();
                                    String peerType = splitResponse[0];

                                    if(splitResponse.length == 2)
                                    {
                                        peerIP += ":" + splitResponse[1];
                                    }

                                    peerRoutingTable[i][0] = peerIP;
                                    peerRoutingTable[i][1] = peerType;
                                    peerRoutingTable[i][2] = peerSocket;

                                    peerReader.close();

                                    System.out.println(HEADER + ": connected to" + peerType + " peer at " + peerIP);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace(System.out);
                                }
                            }

                            try
                            {
                                peerListener.close();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace(System.out);
                            }
                        }

                        break;
                    case COMMAND_START: // args: [requestPort]
                        // wait for both ServerRouters to be ready
                        try
                        {
                            PrintWriter readyWriter = new PrintWriter(otherServerRouter.getOutputStream(), true);
                            BufferedReader readyReader = new BufferedReader(new InputStreamReader(otherServerRouter.getInputStream()));

                            System.out.println(HEADER + ": waiting for other ServerRouter to be ready");
                            readyWriter.println(Protocol.MESSAGE_SR_READY);

                            String response = readyReader.readLine();
                            System.out.println(HEADER + ": received " + response + ", starting!");

                            readyWriter.close();
                            readyReader.close();
                            otherServerRouter.close();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.out);
                        }

                        //pass args to request threads
                        HandleRequestThread.otherServerRouterIP = otherServerRouterIP;
                        HandleRequestThread.otherServerRouterPort = otherServerRouterPort;

                        //start peer request listener
                        ServerSocket requestListener = null;
                        int requestPort = Integer.parseInt(command[1]);

                        try
                        {
                            requestListener = new ServerSocket(requestPort);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.out);
                        }

                        //notify peers that system is ready
                        for (int i = 0; i < peerRoutingTable.length; i++)
                        {
                            try
                            {
                                PrintWriter peerWriter = new PrintWriter(((Socket) peerRoutingTable[i][2]).getOutputStream(), true);
                                peerWriter.println(Protocol.MESSAGE_SR_START);

                                peerWriter.close();
                                ((Socket) peerRoutingTable[i][2]).close();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace(System.out);
                            }
                        }

                        //handle peer requests
                        if (requestListener != null)
                        {
                            while (true)
                            {
                                try
                                {
                                    Socket requestPeer = requestListener.accept();
                                    String peerIP = requestPeer.getInetAddress().getHostAddress();

                                    System.out.println(HEADER + ": got connection from " + peerIP);

                                    HandleRequestThread thread = new HandleRequestThread(peerRoutingTable, requestPeer);
                                    thread.start();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace(System.out);
                                }
                            }
                        }

                        break;
                    case COMMAND_HELP:
                        //TODO: finish
                        if (command.length == 1)
                        {
                            System.out.println(HEADER + ": " + COMMAND_CONNECT_SR + " [ip address] [port]");
                            System.out.println(HEADER + ": " + COMMAND_LISTEN_SR + " [port] [timeout (seconds)]");
                            System.out.println(HEADER + ": " + COMMAND_ACCEPT_PEERS + " [num_peers] [port]");
                            System.out.println(HEADER + ": " + COMMAND_START);
                            System.out.println(HEADER + ": " + COMMAND_HELP);
                        }
                        else
                        {
                            if (command[1].equals(COMMAND_CONNECT_SR))
                            {
                                System.out.println(HEADER + ": " + COMMAND_CONNECT_SR + " [ip address] [port]");
                                System.out.println(HEADER + ": attempts to connect to the other ServerRouter located at");
                                System.out.println(HEADER + ": [ip_address]:[port]. If fails, prints the exception that");
                                System.out.println(HEADER + ": occured.");
                            }
                            else if (command[1].equals(COMMAND_LISTEN_SR))
                            {
                                System.out.println(HEADER + ": " + COMMAND_LISTEN_SR + " [port] [timeout (seconds)]");
                                System.out.println(HEADER + ": Listens for a ServerRouter connection on port [port] for a");
                                System.out.println(HEADER + ": [timeout] seconds. If fails, prints the exception that occured");
                            }
                            else if (command[1].equals(COMMAND_ACCEPT_PEERS))
                            {
                                System.out.println(HEADER + ": " + COMMAND_ACCEPT_PEERS + " [numPeers] [port]");
                                System.out.println(HEADER + ": Accepts connections for peers, limited to ");
                            }
                            else if (command[1].equals(COMMAND_START))
                            {

                            }
                            else if (command[1].equals(COMMAND_HELP))
                            {

                            }
                            else
                            {
                                System.out.println(HEADER + ": no such command " + command[1]);
                            }
                        }

                        break;
                    default:
                        System.out.println(HEADER + ": Unrecognized command");
                        break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
    }
}