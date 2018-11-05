import java.io.*;
import java.net.*;

import java.nio.charset.Charset;
import java.util.Random;

public class TCPClient
{
    static Random rand = new Random();

    public static void main(String[] args) throws IOException
    {

        // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading form ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Client machine's IP
        String routerName = "localhost"; // ServerRouter host name
        int SockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try
        {
            Socket = new Socket(routerName, SockNum);
            out = new PrintWriter(Socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
        } catch (UnknownHostException e)
        {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        // Variables for message passing
        Reader reader = new FileReader("file.txt");
        BufferedReader fromFile = new BufferedReader(reader); // reader for the string file
        String fromServer; // messages received from ServerRouter
        String fromUser; // messages sent to ServerRouter
        String address = "localhost"; // destination IP (Server)

        // Communication process (initial sends/receives
        out.println(address);// initial send (IP of the destination Server)
        fromServer = in.readLine();//initial receive from router (verification of connection)
        System.out.println("ServerRouter: " + fromServer);
        out.println(host); // Client sends the IP of its machine as initial send

        //wait for initial response
        in.readLine();

        BufferedWriter log = new BufferedWriter(new FileWriter(new File("log.csv")));
        log.append("m_size,index,t_send,t_recv,diff");

        for(int size = 1; size <= 10000000; size *= 10)
        {
            for(int index = 0; index < 1000; index++)
            {
                String msg = rand_string(size);
                long t0 = System.currentTimeMillis();

                //send message
                out.println(msg);

                //wait
                in.readLine();

                long t1 = System.currentTimeMillis();

                log.append("" + size + "," + index + "," + t0 + "," + t1 + "," + (t1 - t0));
            }
        }

        // closing connections
        out.close();
        in.close();
        Socket.close();
    }

    public static String rand_string(int length)
    {
        byte[] arr = new byte[length];
        rand.nextBytes(arr);

        return new String(arr, Charset.forName("UTF-8"));
    }
}