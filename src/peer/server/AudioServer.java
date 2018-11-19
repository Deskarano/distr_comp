package peer.server;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class AudioServer extends AbstractServerPeer
{
    private static final String HEADER = "(AudioServer)";
    public AudioServer()
    {
        super("audio");
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

                        System.out.println(HEADER + ": receiving file of size " + recvSize);
                        if(recvSize == 0)
                        {
                            break;
                        }

                        byte[] inputByteBuffer = new byte[8192];
                        File inputFile = new File("in.mp3");
                        FileOutputStream fileOutputStream = new FileOutputStream(inputFile);

                        int receivedBytes = 0;
                        while(receivedBytes != recvSize)
                        {
                            int chunkSize = clientInputStream.read(inputByteBuffer, 0, 8192);
                            receivedBytes += chunkSize;

                            System.out.println(HEADER + ": received chunk of size " + chunkSize + ", receivedBytes = " + receivedBytes);
                            fileOutputStream.write(inputByteBuffer, 0, chunkSize);
                        }

                        System.out.println(HEADER + ": received audio data, converting");

                        fileOutputStream.flush();
                        fileOutputStream.close();

                        System.out.println(HEADER + ": flushed fileOutputStream");

                        AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile);
                        AudioFormat baseFormat = inputStream.getFormat();

                        System.out.println(HEADER + ": got input AudioStream");

                        AudioFormat convertedFormat = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                baseFormat.getSampleRate(),
                                16,
                                baseFormat.getChannels(),
                                baseFormat.getChannels() * 2,
                                baseFormat.getSampleRate(),
                                false);

                        AudioInputStream samples = AudioSystem.getAudioInputStream(convertedFormat, inputStream);

                        System.out.println(HEADER + ": got samples from file, writing out");

                        File outputFile = new File("out.wav");
                        AudioSystem.write(samples, AudioFileFormat.Type.WAVE, outputFile);

                        inputStream.close();
                        samples.close();

                        System.out.println(HEADER + ": done converting file");

                        FileInputStream fileInputStream = new FileInputStream(outputFile);

                        System.out.println(HEADER + ": output file has size " + outputFile.length());

                        byte[] sendSize = ByteBuffer.allocate(4).putInt((int) outputFile.length()).array();
                        clientOutputStream.write(sendSize);

                        byte[] outputByteBuffer = new byte[8192];
                        int sentBytes = 0;

                        while(sentBytes != outputFile.length())
                        {
                            int chunkSize = fileInputStream.read(outputByteBuffer, 0, 8192);
                            sentBytes += chunkSize;

                            System.out.println(HEADER + ": sent chunk of size " + chunkSize + ", sentBytes = " + sentBytes);

                            clientOutputStream.write(outputByteBuffer, 0, chunkSize);
                        }

                        clientOutputStream.flush();
                        fileInputStream.close();
                    }

                    clientInputStream.close();
                    clientOutputStream.close();
                }
                catch(UnsupportedAudioFileException e)
                {
                    e.printStackTrace(System.out);
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.out);
                }
            }).start();
        }
    }
}
