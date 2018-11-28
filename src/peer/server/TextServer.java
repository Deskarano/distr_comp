package peer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TextServer extends AbstractServerPeer
{
    private static final String HEADER = "(TextServer)";

    public TextServer()
    {
        super("text");
    }

    public void run(int port)
    {
        listenOnPort(port);

        while(true)
        {
            final Socket client = getClient();

            System.out.println(HEADER + ": received connection from " + client.getInetAddress().getHostAddress());

            new Thread(() ->
            {
                try
                {
                    BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter clientWriter = new PrintWriter(client.getOutputStream(), true);

                    String line;
                    while((line = clientReader.readLine()) != null)
                    {
                        System.out.println(HEADER + " " + client.getInetAddress().getHostAddress() + " sent " + line);

                        long processStartTime = System.currentTimeMillis();
                        String response = line.toUpperCase();
                        long processEndTime = System.currentTimeMillis();

                        System.out.println("(DATA): processing time was " + (processEndTime - processStartTime) + "ms");
                        System.out.println(HEADER + " " + client.getInetAddress().getHostAddress() + " responding " + response);

                        clientWriter.println(response);
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace(System.out);
                }
            }).start();
        }
    }
}
