package or.kr.formulate.korail.dummy.exam;

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

public class Cms0600Server {
    private static final Logger logger = LoggerFactory.getLogger(Cms0600Server.class);


    ExecutorService executorService;
    final int capacity = 1024;
    final int port = 8000;
    final int clientPort = 8080;

    private void createServer() {
        executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            // Runnable Task로 지정된 클래스 수행
            System.out.println("Server execute service started");
        });
        executorService.submit(() -> {
            // Callable Task로 지정된 클래스(: 리턴값 있는 경우) 수행
            System.out.println("Server submit service started");
        });

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            InetSocketAddress address = new InetSocketAddress("localhost", port);
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(address);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        System.out.println("New TCP Connection");

                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel1.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
                        int bytesRead = socketChannel.read(byteBuffer);

                        if (bytesRead == -1) {
                            System.out.println("TCP connection closed");
                            key.cancel();
                        } else {
                            byteBuffer.flip();
                            byte[] content = new byte[bytesRead];
                            byteBuffer.get(content, 0, bytesRead);
                            System.out.println("Received: \n[" + new String(content, StandardCharsets.UTF_8) + "]");

                            ByteBuffer message = ByteBuffer.wrap(content);
                            // client로부터 수신 된 데이터를 그대로 응답
                            socketChannel.write(message);
                        }

                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startClient() throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", port));
        socketChannel.configureBlocking(false);
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
        Cms0600Server ex = new Cms0600Server();
        ex.createServer();
//
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

//        try {
//            ex.startClient();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }


    }
}
