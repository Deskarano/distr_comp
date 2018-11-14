package peer.client;

import protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TextClient extends AbstractClientPeer
{
    public static final String HEADER = "(TextClient)";
    public TextClient()
    {
        super("text");
    }

    public void run()
    {
        Socket server = connectToServer("text", "other");

        try
        {
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            
            Scanner input = new Scanner(System.in);

            while(true)
            {
                System.out.print(HEADER + " >>> ");

                String message = input.nextLine();
                System.out.println(HEADER + ": sending message " + message);

                writer.println(message);

                String response = reader.readLine();
                System.out.println(HEADER + ": received response " + response);
            }
        }
        catch (IOException e)
        {

        }
    }
}
