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
                        // receive input size
                        byte[] recvSizeBytes = new byte[4];

                        clientInputStream.read(recvSizeBytes);
                        int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                        System.out.println(HEADER + ": receiving file of size " + recvSize);
                        if(recvSize == 0)
                        {
                            break;
                        }

                        // receive input data
                        File inputFile = new File("in.mp3");
                        FileOutputStream fileOutputStream = new FileOutputStream(inputFile);
                        transferStreams(recvSize, 8192, clientInputStream, fileOutputStream);
                        fileOutputStream.close();

                        System.out.println(HEADER + ": received audio data, converting");

                        // convert the file: first get input properties
                        AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile);
                        AudioFormat baseFormat = inputStream.getFormat();

                        System.out.println(HEADER + ": got input AudioStream");

                        // then create output properties and stream
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

                        // write out the converted data
                        File outputFile = new File("out.wav");
                        AudioSystem.write(samples, AudioFileFormat.Type.WAVE, outputFile);

                        inputStream.close();
                        samples.close();

                        inputFile.delete();
                        outputFile.delete();

                        System.out.println(HEADER + ": done converting file");

                        // send size of output file
                        System.out.println(HEADER + ": output file has size " + outputFile.length() + ", sending data");

                        byte[] sendSize = ByteBuffer.allocate(4).putInt((int) outputFile.length()).array();
                        clientOutputStream.write(sendSize);

                        // send output data
                        FileInputStream fileInputStream = new FileInputStream(outputFile);
                        transferStreams((int) outputFile.length(), 8192, fileInputStream, clientOutputStream);
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
