package or.kr.formulate.korail.util.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cms0600Client {
    private static final Logger logger = LoggerFactory.getLogger(Cms0600Client.class);


    ExecutorService executorService;
    final int capacity = 1024;
    final int port = 8000;
    final int clientPort = 8080;


    private void startClient() throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", port));
        socketChannel.configureBlocking(true);
        System.out.println("Socket connection established");

        final Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);

        new Thread(() -> {
            while (true) {
                try {
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for (Iterator<SelectionKey> iterator = selectionKeys.iterator(); iterator.hasNext(); ) {
                        SelectionKey key = iterator.next();

                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
                            int bytesRead = sc.read(byteBuffer);

                            if (bytesRead == -1) {
                                System.out.println("TCP connection closed");
                                key.cancel();
                            } else {
                                byteBuffer.flip();
                                byte[] content = new byte[bytesRead];
                                byteBuffer.get(content, 0, bytesRead);

                                System.out.println("Received: \n[" + new String(content, StandardCharsets.UTF_8) + "]");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        }, "selector thread").start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();

            if (Objects.equals(line, "exit")) break;

            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            buffer.clear();
            buffer.put(line.getBytes(StandardCharsets.UTF_8));
            buffer.flip();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }

        socketChannel.close();
        System.out.println("TCP connection closed");
    }

    public static void main(String[] args) {
        Cms0600Client ex = new Cms0600Client();
        try {
            ex.startClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //
        }

    }
}
