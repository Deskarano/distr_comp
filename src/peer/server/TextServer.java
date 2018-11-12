package peer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TextServer extends AbstractServerPeer
{
    public TextServer()
    {
        super("text");
    }

    public void run()
    {
        Socket client = getClient();

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println(reader.readLine());
        }
        catch (IOException e)
        {

        }
    }
}
