package or.kr.formulate.korail.util.cms.pool2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Server0320 {

    private static final int BATCH_SIZE = 100;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final int serverPort = 7777;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

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
