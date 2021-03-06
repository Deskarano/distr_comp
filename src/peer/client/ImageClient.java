package peer.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class ImageClient extends AbstractClientPeer
{
    private static final String HEADER = "(ImageClient)";

    private static final String COMMAND_CONNECT = "connect";
    private static final String COMMAND_DISCONNECT = "disconnect";
    private static final String COMMAND_SEND_IMAGE = "image";
    private static final String COMMAND_EXIT = "exit";

    public ImageClient()
    {
        super("image");
    }

    private Socket connectToServer(String from)
    {
        return super.connectToServer("image", from);
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
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_SEND_IMAGE: // args: [source_file] [dest_file]
                        try
                        {
                            BufferedImage sendImage = ImageIO.read(new File(command[1]));

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ImageIO.write(sendImage, "jpg", byteArrayOutputStream);

                            System.out.println(HEADER + ": image " + command[1] + " has size " + byteArrayOutputStream.size());

                            byte[] sendSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();

                            long sendStart = System.currentTimeMillis();
                            serverOutputStream.write(sendSize);
                            serverOutputStream.write(byteArrayOutputStream.toByteArray());
                            serverOutputStream.flush();
                            long sendEnd = System.currentTimeMillis();

                            System.out.println("(DATA): image sent in " + (sendEnd - sendStart) + "ms");
                            byteArrayOutputStream.close();

                            System.out.println(HEADER + ": sent image, waiting for response size");

                            long waitStart = System.currentTimeMillis();
                            byte[] recvSizeBytes = new byte[4];
                            serverInputStream.read(recvSizeBytes);
                            int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();
                            long waitEnd = System.currentTimeMillis();

                            System.out.println("(DATA): waiting for response took " + (waitEnd - waitStart) + "ms");

                            System.out.println(HEADER + ": response image has size " + recvSize);

                            byte[] imageBytes = new byte[recvSize];

                            long recvStart = System.currentTimeMillis();
                            int receivedBytes = 0;
                            while(receivedBytes != recvSize)
                            {
                                int chunkSize = serverInputStream.read(imageBytes, receivedBytes, recvSize - receivedBytes);
                                receivedBytes += chunkSize;

                                System.out.println(HEADER + ": received chunk of size " + chunkSize + ", receivedBytes = " + receivedBytes);
                            }
                            long recvStop = System.currentTimeMillis();

                            System.out.println("(DATA): received response data in " + (recvStop - recvStart) + "ms");

                            System.out.println(HEADER + ": received response image data");

                            BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            ImageIO.write(receivedImage, "jpg", new File(command[2]));

                            System.out.println(HEADER + ": saved response image as " + command[2]);
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
