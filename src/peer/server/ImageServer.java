package peer.server;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ImageServer extends AbstractServerPeer
{
    private static final String HEADER = "(ImageServer)";
    public ImageServer()
    {
        super("image");
    }

    @Override
    public void run(int port)
    {
        listenOnPort(port);

        while(true)
        {
            final Socket client = getClient();

            System.out.println(HEADER + ": received connection from " + client.getInetAddress().getHostAddress());

            new Thread(() ->
            {
                try
                {
                    InputStream inputStream = client.getInputStream();
                    OutputStream outputStream = client.getOutputStream();

                    while(true)
                    {
                        byte[] recvSizeBytes = new byte[4];

                        inputStream.read(recvSizeBytes);
                        int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                        System.out.println(HEADER + ": receiving image of size " + recvSize);
                        if(recvSize == 0)
                        {
                            break;
                        }

                        byte[] imageBytes = new byte[recvSize];

                        int receivedBytes = 0;
                        while(receivedBytes != recvSize)
                        {
                            int chunkSize = inputStream.read(imageBytes, receivedBytes, recvSize - receivedBytes);
                            receivedBytes += chunkSize;

                            System.out.println(HEADER + ": received chunk of size " + chunkSize + ", receivedBytes = " + receivedBytes);
                        }

                        System.out.println(HEADER + ": received image data, converting");

                        BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                        ImageIO.write(receivedImage, "jpg", new File("received.jpg"));

                        ImageFilter filter = new GrayFilter(true, 50);
                        ImageProducer producer = new FilteredImageSource(receivedImage.getSource(), filter);
                        Image rendered = Toolkit.getDefaultToolkit().createImage(producer);

                        BufferedImage responseImage = new BufferedImage(rendered.getWidth(null), rendered.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
                        responseImage.getGraphics().drawImage(rendered, 0, 0, null);
                        responseImage.getGraphics().dispose();

                        System.out.println(HEADER + ": done converting image");

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(responseImage, "jpg", byteArrayOutputStream);

                        System.out.println(HEADER + ": response image has size " + byteArrayOutputStream.size());

                        byte[] sendSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                        outputStream.write(sendSize);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.flush();

                        System.out.println(HEADER + ": sent response image data");
                    }

                    inputStream.close();
                    outputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.out);
                }
            }).start();
        }
    }
}
