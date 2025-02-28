package or.kr.formulate.korail.util.cms.pool2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Client0320 {

    private static final Logger logger = LoggerFactory.getLogger(Client0320.class);

    private static final int BATCH_SIZE = 100;
    private static final Charset EUC_KR = Charset.forName("EUC-KR");
    private static final int serverPort = 7777;
    private static final String serverHost = "localhost";

    public void start(String filePath) {
        try (Socket socket = new Socket(serverHost, serverPort);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            logger.debug("start");
            String line;
            int recordCount = 0;

            while ((line = reader.readLine()) != null) {
                byte[] recordBytes = line.getBytes(EUC_KR);
                logger.debug("Record : {}", line);
                sendRecord(dos, recordBytes);
                recordCount++;

                if (recordCount % BATCH_SIZE == 0) {
                    sendAckRequest(dos);
                    String response = receiveAckResponse(dis);
                    if (!"ACK_OK".equals(response)) {
                        // 재전송 또는 오류 처리
                    }
                }
            }

            // 마지막 배치 처리
            if (recordCount % BATCH_SIZE != 0) {
                sendAckRequest(dos);
                String response = receiveAckResponse(dis);
                if (!"ACK_OK".equals(response)) {
                    // 재전송 또는 오류 처리
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRecord(DataOutputStream dos, byte[] recordBytes) throws IOException {
        dos.writeInt(recordBytes.length);
        dos.write(recordBytes);
    }

    private void sendAckRequest(DataOutputStream dos) throws IOException {
        dos.writeUTF("ACK_REQUEST");
        dos.flush();
    }

    private String receiveAckResponse(DataInputStream dis) throws IOException {
        return dis.readUTF();
    }
}

