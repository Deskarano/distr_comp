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
        Socket client = getClient();

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

            while(true)
            {
                String response = reader.readLine();

                System.out.println(HEADER + ": received message" + response);
                System.out.println(HEADER + ": responding with " + response.toUpperCase());

                writer.println(response.toUpperCase());
            }
        }
        catch (IOException e)
        {

        }
    }
}
