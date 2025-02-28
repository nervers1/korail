package or.kr.formulate.korail.util.cms.nonblock;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

public class Client1 {
    private static final Logger logger = LoggerFactory.getLogger(Client1.class);
    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");
    private static final String encoding = prop.getProperty("Server.ENCODING");

    // 서버 연결 설정
    private static String SERVER_ADDRESS = "localhost";
    private static int SERVER_PORT = 7777;
    private static final int TIMEOUT = 30000;
    private static final int RETRY_COUNT = 3;

    public Client1(String host, int port) {
        SERVER_ADDRESS = host;
        SERVER_PORT = port;
    }

    public static void main(String[] args) {

        try (Socket socket = new Socket()) {
            // 소켓 연결 (타임아웃 적용)
            socket.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT), TIMEOUT);

            // 읽기 타임아웃 설정
            socket.setSoTimeout(TIMEOUT);

            try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                // 1. 데이터 생성 및 송신
                byte[] requestData = createRequestData("This Message is very important data from Client1");
                boolean isSent = sendDataWithRetry(socket, output, requestData, RETRY_COUNT);

                if (!isSent) {
                    logger.error("데이터 송신 실패: 최대 재시도 횟수 초과. 소켓을 닫습니다.");
                    socket.close();
                    return;
                }

                // 2. 응답 수신
                try {
                    if (socket.isConnected() && !socket.isInputShutdown()) {
                        byte[] responseData = receiveData(input);
                        String parsedResponse = parseResponse(responseData);
                        logger.debug("응답 데이터: {}", parsedResponse);
                    } else {
                        logger.error("서버 응답 수신 실패: 소켓 연결 종료.");
                    }
                } catch (SocketTimeoutException e) {
                    logger.error("서버 응답 시간 초과: {}", e.getMessage());
                }

            } catch (IOException e) {
                logger.error("송수신 중 오류 발생: {}", e.getMessage());
            }
        } catch (IOException e) {
            logger.error("소켓 연결 실패: {}", e.getMessage());
        }
    }

    private static byte[] createRequestData(String message) {
        try {
            return message.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("인코딩 오류 발생: {}", e.getMessage());
            return new byte[0];
        }
    }

    private static boolean sendDataWithRetry(Socket socket, DataOutputStream output, byte[] data, int retryCount) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                if (socket.isConnected() && !socket.isOutputShutdown()) {
                    logger.debug("데이터 송신 시도 (시도 횟수: {})", attempts + 1);
                    sendData(output, data);
                    logger.debug("데이터 송신 성공");
                    return true;
                } else {
                    logger.error("소켓 연결 상태 확인 실패. 송신 시도 중단.");
                }
            } catch (IOException e) {
                attempts++;
                logger.warn("데이터 송신 오류 발생: {}. 재시도 중... (시도 횟수: {}/{})", e.getMessage(), attempts, retryCount);
                if (attempts >= retryCount) {
                    logger.error("최대 재시도 횟수 초과. 송신 실패.");
                    return false;
                }
                // 재시도 전에 약간의 대기 시간 추가
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    logger.error("재시도 대기 중 인터럽트 발생.");
                    return false;
                }
            }
        }
        return false;
    }

    private static void sendData(DataOutputStream output, byte[] data) throws IOException {
        logger.debug("데이터 송신 준비 (길이: {})", data.length);
        output.writeInt(data.length);
        output.write(data);
        output.flush();
        logger.debug("서버로 데이터 송신 완료: {}", new String(data, encoding));
    }

    private static byte[] receiveData(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length <= 0 || length > 1024 * 1024) { // 최대 1MB 제한
            logger.error("잘못된 데이터 길이 수신: {}", length);
            return new byte[0];
        }
        byte[] data = new byte[length];
        input.readFully(data);
        logger.debug("서버에서 데이터 수신 완료 (길이: {})", length);
        return data;
    }

    private static String parseResponse(byte[] response) {
        try {
            return new String(response, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("응답 파싱 중 인코딩 오류 발생: {}", e.getMessage());
            return "";
        }
    }
}
