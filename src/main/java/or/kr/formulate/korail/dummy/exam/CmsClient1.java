package or.kr.formulate.korail.dummy.exam;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class CmsClient1 implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CmsClient1.class.getName());
    @Override
    public void run() {
        SocketChannel socketChannel = null;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            System.out.println( "[연결 요청]");
            socketChannel.connect(new InetSocketAddress("localhost", 5001));
            System.out.println( "[연결 성공]");

            ByteBuffer byteBuffer = null;
            Charset charset = Charset.forName("UTF-8");

            byteBuffer = charset.encode("Hello Server");
            socketChannel.write(byteBuffer);
            System.out.println( "[데이터 보내기 성공]");

            byteBuffer = ByteBuffer.allocate(100);
            int byteCount = socketChannel.read(byteBuffer);
            byteBuffer.flip();
            String message = charset.decode(byteBuffer).toString();
            System.out.println("[데이터 받기 성공]: " + message);

        } catch(Exception e) {
            //
        }

        if(socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e1) {}
        }

    }
}
