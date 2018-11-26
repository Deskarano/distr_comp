package peer.client;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class AudioClient extends AbstractClientPeer
{
    private static final String HEADER = "(AudioClient)";

    private static final String COMMAND_CONNECT = "connect";
    private static final String COMMAND_DISCONNECT = "disconnect";
    private static final String COMMAND_SEND_AUDIO = "audio";
    private static final String COMMAND_EXIT = "exit";

    public AudioClient()
    {
        super("audio");
    }

    private Socket connectToServer(String from)
    {
        return super.connectToServer("audio", from);
    }

    @Override
    public void run()
    {
        Scanner input = new Scanner(System.in);
        Socket server = null;

        InputStream serverInputStream = null;
        OutputStream serverOutputStream = null;

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
                            serverInputStream = server.getInputStream();
                            serverOutputStream = server.getOutputStream();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_DISCONNECT:
                        try
                        {
                            serverOutputStream.write(new byte[]{0, 0, 0, 0});
                            serverOutputStream.flush();

                            if (server != null)
                            {
                                server.close();
                            }

                            System.out.println(HEADER + ": disconnected");
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_SEND_AUDIO: // args: [source_file] [dest_file]
                        try
                        {
                            // read input file
                            File audioFile = new File(command[1]);

                            System.out.println(HEADER + ": file " + command[1] + " has size " + audioFile.length() + ", sending data");

                            // send size of input data
                            byte[] sendSizeBytes = ByteBuffer.allocate(4).putInt((int) audioFile.length()).array();
                            serverOutputStream.write(sendSizeBytes);

                            // send input data
                            FileInputStream fileInputStream = new FileInputStream(audioFile);
                            transferStreams((int) audioFile.length(), 8192, fileInputStream, serverOutputStream);
                            fileInputStream.close();

                            System.out.println(HEADER + ": sent file, waiting for converted size");

                            // receive output size
                            byte[] recvSizeBytes = new byte[4];
                            serverInputStream.read(recvSizeBytes);
                            int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                            System.out.println(HEADER + ": converted file has size " + recvSize);

                            // receive output data
                            File resultFile = new File(command[2]);
                            FileOutputStream fileOutputStream = new FileOutputStream(resultFile);
                            transferStreams(recvSize, 8192, serverInputStream, fileOutputStream);

                            System.out.println(HEADER + ": received converted data, writing to file");
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
