package peer.server;

import java.net.Socket;

public class TextServer extends AbstractServerPeer
{
    public TextServer()
    {
        super("text");
    }

    public void run()
    {
        registerToServerRouter("8.8.8.8", 25565);
        listenOnPort(25565);

        while(true)
        {
            Socket client = getClient();

            //communicate with client
        }
    }
}
