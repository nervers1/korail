package or.kr.formulate.korail.util.cms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class CmsClient {
    static SocketChannel socketChannel = null;

    public static void main(String[] args) {

        try {
            // SocketChannel을 생성하고, 몇 가지 설정을 한다.
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);

            // 서버에 연결하기.
            socketChannel.connect(new InetSocketAddress("localhost", 10000));

            // 마찬가지로, 연결된 서버와 입/출력해보기.
            Charset charset = Charset.forName("UTF-8");

            ByteBuffer byteBuffer = charset.encode("Hello, Server !");
            socketChannel.write(byteBuffer);
            System.out.println("Sending Success");

            byteBuffer = ByteBuffer.allocate(128);
            socketChannel.read(byteBuffer);
            System.out.println("Received : " + charset.decode(byteBuffer).toString());

            // 서버와 볼일이 끝났으면, 소켓 닫기.
            if (socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
