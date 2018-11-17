package peer.client;

import javax.imageio.ImageIO;
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
                            serverOutputStream.write(sendSize);
                            serverOutputStream.write(byteArrayOutputStream.toByteArray());
                            serverOutputStream.flush();

                            System.out.println(HEADER + ": sent image, waiting for response size");

                            byte[] recvSizeBytes = new byte[4];
                            serverInputStream.read(recvSizeBytes);
                            int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                            System.out.println(HEADER + ": response image has size " + recvSize);

                            byte[] imageBytes = new byte[recvSize];
                            serverInputStream.read(imageBytes);

                            System.out.println(HEADER + ": received response image data");

                            BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            ImageIO.write(receivedImage, "jpg", new File(command[2]));

                            System.out.println(HEADER + ": saved response image as " + command[2]);
                        }
                        catch (IOException e)
                        {

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
