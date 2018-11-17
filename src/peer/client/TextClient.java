package peer.client;

import protocol.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TextClient extends AbstractClientPeer
{
    private static final String HEADER = "(TextClient)";

    private static final String COMMAND_CONNECT = "connect";
    private static final String COMMAND_DISCONNECT = "disconnect";
    private static final String COMMAND_SEND_FILE = "file";
    private static final String COMMAND_SEND_STR = "string";
    private static final String COMMAND_EXIT = "exit";

    public TextClient()
    {
        super("text");
    }

    private Socket connectToServer(String from)
    {
        return super.connectToServer("text", from);
    }

    public void run()
    {
        Scanner input = new Scanner(System.in);
        Socket server = null;

        BufferedReader serverReader = null;
        PrintWriter serverWriter = null;

        while (true)
        {
            try
            {
                System.out.print(HEADER + " >>> ");
                String read = input.nextLine();

                String[] command = read.split(" ");

                switch (command[0])
                {
                    case COMMAND_CONNECT: // args: [from]
                        server = connectToServer(command[1]);

                        try
                        {
                            serverReader = new BufferedReader(new InputStreamReader(server.getInputStream()));
                            serverWriter = new PrintWriter(server.getOutputStream(), true);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_DISCONNECT:
                        try
                        {
                            if (server != null)
                            {
                                serverReader.close();
                                serverWriter.close();
                                server.close();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_SEND_FILE: // args: [from]
                        File f = new File(command[1]);

                        try
                        {
                            BufferedReader fileReader = new BufferedReader(new FileReader(f));

                            String line;
                            while ((line = fileReader.readLine()) != null)
                            {
                                System.out.println(HEADER + ": sending line " + line);
                                serverWriter.println(line);

                                String response = serverReader.readLine();
                                System.out.println(HEADER + ": received response " + response);
                            }

                            fileReader.close();
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace(System.out);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }
                        break;

                    case COMMAND_SEND_STR:
                        String str = command[1];

                        try
                        {
                            System.out.println(HEADER + ": sending message " + str);
                            serverWriter.println(str);

                            String response = serverReader.readLine();
                            System.out.println(HEADER + ": received response " + response);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_EXIT:
                        try
                        {
                            if (server != null)
                            {
                                server.close();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        return;
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace(System.out);
            }
        }
    }
}
