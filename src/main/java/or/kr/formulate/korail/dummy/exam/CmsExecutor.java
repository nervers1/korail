package or.kr.formulate.korail.dummy.exam;

import or.kr.formulate.korail.code.ResponseCode;
import or.kr.formulate.korail.exception.EAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class CmsExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CmsExecutor.class);

    public CmsExecutor() {}

    public static void server(String hostName, int portNumber) throws IOException {
        try (CmsServerSocket serverSocket = new CmsServerSocket()) {
            SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
            serverSocket.bind(socketAddress);
            logger.debug("Starting TCP Server( address: [{}], port: {} )...", socketAddress, serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("Connected {}, From {}", socket.getLocalPort(), socket.getRemoteSocketAddress().toString());
                Server tcpServer = new Server(socket);
                tcpServer.start();
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EAIException(e.getMessage(), ResponseCode.RES_090, e);
        }
    }

    public static void client(String hostName, int portNumber) throws IOException {
        try (CmsSocket socket = new CmsSocket();

        ) {
            socket.connect(new InetSocketAddress(hostName, portNumber));
            logger.debug("Connected {}, From {}", socket.getRemoteSocketAddress(), socket.getLocalSocketAddress());

        }
    }

}
