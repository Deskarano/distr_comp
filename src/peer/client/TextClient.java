package peer.client;

import protocol.Protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TextClient extends AbstractClientPeer
{
    public TextClient()
    {
        super("text");
    }

    public void run()
    {
        Socket server = connectToServer("text", "near");

        try
        {
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);

            writer.println("test");
        }
        catch (IOException e)
        {

        }
    }
}
