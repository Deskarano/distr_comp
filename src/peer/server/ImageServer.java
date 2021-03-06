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
                    InputStream clientInputStream = client.getInputStream();
                    OutputStream clientOutputStream = client.getOutputStream();

                    while(true)
                    {
                        byte[] recvSizeBytes = new byte[4];

                        clientInputStream.read(recvSizeBytes);
                        int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                        System.out.println(HEADER + ": receiving image of size " + recvSize);
                        if(recvSize == 0)
                        {
                            break;
                        }

                        byte[] imageBytes = new byte[recvSize];

                        long recvStart = System.currentTimeMillis();
                        int receivedBytes = 0;
                        while(receivedBytes != recvSize)
                        {
                            int chunkSize = clientInputStream.read(imageBytes, receivedBytes, recvSize - receivedBytes);
                            receivedBytes += chunkSize;

                            System.out.println(HEADER + ": received chunk of size " + chunkSize + ", receivedBytes = " + receivedBytes);
                        }
                        long recvEnd = System.currentTimeMillis();

                        System.out.println("(DATA): receiving image took " + (recvEnd - recvStart) + "ms");

                        System.out.println(HEADER + ": received image data, converting");

                        long convertStart = System.currentTimeMillis();
                        BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        ImageFilter filter = new GrayFilter(true, 50);
                        ImageProducer producer = new FilteredImageSource(receivedImage.getSource(), filter);
                        Image rendered = Toolkit.getDefaultToolkit().createImage(producer);

                        BufferedImage responseImage = new BufferedImage(rendered.getWidth(null), rendered.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
                        responseImage.getGraphics().drawImage(rendered, 0, 0, null);
                        responseImage.getGraphics().dispose();
                        long convertEnd = System.currentTimeMillis();

                        System.out.println("(DATA): image conversion took " + (convertEnd - convertStart) + "ms");

                        System.out.println(HEADER + ": done converting image");

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(responseImage, "jpg", byteArrayOutputStream);

                        System.out.println(HEADER + ": response image has size " + byteArrayOutputStream.size());

                        long sendStart = System.currentTimeMillis();
                        byte[] sendSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                        clientOutputStream.write(sendSize);
                        clientOutputStream.write(byteArrayOutputStream.toByteArray());
                        clientOutputStream.flush();
                        long sendEnd = System.currentTimeMillis();

                        System.out.println("(DATA): sending response image took " + (sendEnd - sendStart) + "ms");

                         byteArrayOutputStream.close();

                        System.out.println(HEADER + ": sent response image data");
                    }

                    clientInputStream.close();
                    clientOutputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.out);
                }
            }).start();
        }
    }
}
