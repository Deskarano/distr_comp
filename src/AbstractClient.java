import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class AbstractClient
{
    private String type;

    public AbstractClient(String type)
    {
        this.type = type;
    }

    public void connectToServerRouter(String IP, int port)
    {
        try
        {
            Socket serverRouter = new Socket(IP, port);

            PrintWriter serverRouterWriter = new PrintWriter(serverRouter.getOutputStream(), true);
            BufferedReader serverRouterReader = new BufferedReader(new InputStreamReader(serverRouter.getInputStream()));

            serverRouterWriter.write(this.type);

        }
        catch(IOException e)
        {

        }
    }
}
