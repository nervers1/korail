package or.kr.formulate.korail.util.cms.pool2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Server0320 {

    private static final Logger logger = LoggerFactory.getLogger(Server0320.class);

    private static final int BATCH_SIZE = 100;
    private static final Charset UTF_8 = Charset.forName("EUC-KR");
    private static final int serverPort = 7777;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            logger.debug("start");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.debug("Connected {}, From {}", clientSocket.getLocalPort(), clientSocket.getRemoteSocketAddress().toString());
                handleClient(clientSocket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            logger.debug("Handling client request...");
            int receivedRecordCount = 0;

            while (true) {
                int recordLength = dis.readInt();
                byte[] recordBytes = new byte[recordLength];
                dis.readFully(recordBytes);

                processRecord(recordBytes);
                receivedRecordCount++;

                if (receivedRecordCount % BATCH_SIZE == 0) {
                    String ackRequest = dis.readUTF();
                    if ("ACK_REQUEST".equals(ackRequest)) {
                        sendAckResponse(dos, "ACK_OK");
                    } else {
                        // 오류 처리
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRecord(byte[] recordBytes) {
        // 레코드 처리 로직 (예: DB 저장)
    }

    private void sendAckResponse(DataOutputStream dos, String response) throws IOException {
        dos.writeUTF(response);
        dos.flush();
    }
}
