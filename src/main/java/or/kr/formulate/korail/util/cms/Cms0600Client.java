package or.kr.formulate.korail.util.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

/**
 * CMS Client : CMS 파일 전송을 위해 파일을 읽어 CMS 서버로 송신한다.
 * - 이때 보내지는 파일은 블럭과 시퀀스로 나누어 전송한다.
 * - 블럭의 갯수는 0001 ~ 9999개 이내, 한 블럭은 1 ~ 100 SEQUENCE로 구성
 * - 시퀀스는 데이터의 1 Row를 기준으로 함(한 블럭은 100건의 데이터, 1 ~ 100개의 SEQUENCE로 구성)
 */
public class Cms0600Client {
    private static final Logger logger = LoggerFactory.getLogger(Cms0600Client.class);

    public static void main(String[] args) {


        try (CmsSocket socket = new CmsSocket("localhost", 8080);
             OutputStream os = socket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);
             InputStream is = socket.getInputStream();
             DataInputStream dis = new DataInputStream(is)) {

            logger.debug("Connected {}, From {}", socket.getLocalPort(), socket.getRemoteSocketAddress().toString());
            socket.connect(new InetSocketAddress("localhost", 8080));

            BufferedInputStream bis = new BufferedInputStream(is);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
