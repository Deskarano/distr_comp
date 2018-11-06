package peer.client;

import protocol.Protocol;

import java.net.Socket;

public class TextClient extends AbstractClientPeer
{
    public TextClient()
    {
        super("text");
    }

    public void run()
    {
        //TODO: generic-ify
        registerToServerRouter("8.8.8.8", 25565);
        Socket server = connectToServer("text", Protocol.REQUEST_FROM_FAR);

        // send stuff over socket, get results back
    }
}
