package or.kr.formulate.korail.util.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CmsServer {
    private static final Logger logger = LoggerFactory.getLogger(CmsServer.class);
    static ServerSocketChannel serverSocketChannel = null;

    public static void main(String[] args) {

        try {
            // ServerSocketChannel을 생성하고, 몇 가지 설정을 한다.
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.bind(new InetSocketAddress(10000));

            while (true) {
                // 블로킹 방식으로 설정했기 때문에, accept() 메서드에서 연결을 받을 때까지 블락된다.
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("Connected : " + socketChannel.getRemoteAddress());

                // 연결된 클라이언트와 입/출력하기.
                Charset charset = Charset.forName("UTF-8");

                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                socketChannel.read(byteBuffer);
                byteBuffer.flip();
                System.out.println("Received Data : " + charset.decode(byteBuffer).toString());

                byteBuffer = charset.encode("Hello, My Client !");
                socketChannel.write(byteBuffer);
                System.out.println("Sending Success");
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void close() {
        if(serverSocketChannel.isOpen()) {
            try {
                serverSocketChannel.close();
            } catch (IOException e1) {
                //
            }
        }
    }
}
