package or.kr.formulate.korail.util.cms.nonblock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client1 {
    private static final Logger logger = LoggerFactory.getLogger(Client1.class);

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int TIMEOUT = 30000;

    public static void main(String[] args) {

        try (Socket socket = new Socket()) {

            // Socket connect 시점에 타임아웃 지정
            socket.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT), TIMEOUT);

            try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                // 1. 송신할 데이터를 생성
                byte[] requestData = createRequestData("Hello from Client1");

                // 2. 송신
                if (socket.isConnected() && !socket.isOutputShutdown()) {
                    sendData(output, requestData);
                } else {
                    logger.error("소켓 출력 스트림이 닫혀 있습니다. 데이터 송신 실패.");
                    return;
                }


                // 3. 응답 수신
                if (socket.isConnected() && !socket.isInputShutdown()) {
                    byte[] responseData = receiveData(input);
                    // 4. 데이터 파싱
                    String parsedResponse = parseResponse(responseData);
                    logger.debug("Server Response: {}", parsedResponse);
                } else {
                    logger.error("서버 응답을 받을 수 없습니다. 연결이 닫혔습니다.");
                }

                // 5. 또 다른 요청 송신(ACK 가 필요한 경우 활용)
                byte[] anotherRequestData = createRequestData("Another request from Client1");
                if (socket.isConnected() && !socket.isOutputShutdown()) {
                    sendData(output, anotherRequestData);
                } else {
                    logger.error("소켓 출력 스트림이 닫혀 있습니다. 추가 데이터 송신 실패.");
                }


            } catch (IOException e) {
                e.printStackTrace();
                logger.error("데이터 송수신 중 오류 발생: {}", e.getMessage());
            }
        } catch (IOException e) {
            logger.error("소켓 연결 중 오류 발생: {}", e.getMessage());
        }
    }

    private static byte[] createRequestData(String message) {
        return message.getBytes();
    }

    private static void sendData(DataOutputStream output, byte[] data) throws IOException {
        logger.debug("Preparing to send data. Length: {}", data.length);

        output.writeInt(data.length);
        output.write(data);
        output.flush();
        logger.debug("Data sent to server: {}", new String(data));
    }

    private static byte[] receiveData(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length <= 0) {
            logger.error("Received invalid data length: {}", length);
            return new byte[0];
        }
        byte[] data = new byte[length];
        input.readFully(data);
        logger.debug("Data received from server. Length: {}", length);
        return data;

    }

    private static String parseResponse(byte[] response) {
        return new String(response);
    }
}
