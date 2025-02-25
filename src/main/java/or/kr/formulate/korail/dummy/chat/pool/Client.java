package or.kr.formulate.korail.dummy.chat.pool;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Properties;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");
    private final String encoding = prop.getProperty("Server.ENCODING");
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    // 클라이언트 시작 메소드
    public void startClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            logger.debug("서버에 연결되었습니다.");

            // 스트림 초기화
            initializeStreams();

            // 데이터 송신 및 수신
            sendData();
            receiveData();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // 스트림 및 소켓 닫기
            closeStreams();
            closeSocket();
        }
    }

    // 스트림 초기화 메소드
    private void initializeStreams() throws IOException {
        outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);

        inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
    }

    // 데이터 송신 메소드
    private void sendData() throws IOException {
        String message = "클라이언트에서 보내는 메시지입니다.";
        byte[] sendBytes = message.getBytes(encoding);
        objectOutputStream.writeObject(sendBytes);
        objectOutputStream.flush();
        logger.debug("서버에게 메시지를 보냈습니다.");
    }

    // 데이터 수신 메소드
    private void receiveData() throws IOException, ClassNotFoundException {
        byte[] receivedBytes = (byte[]) objectInputStream.readObject();
        String receivedMessage = new String(receivedBytes, encoding);
        logger.debug("서버로부터 받은 메시지: [{}]", receivedMessage);
    }

    // 스트림 닫기 메소드
    private void closeStreams() {
        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 소켓 닫기 메소드
    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            logger.debug("소켓이 닫혔습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메인 메소드
    public static void main(String[] args) {
        final int port = Integer.parseInt(prop.getProperty("Server.PORT"));
        final String host = prop.getProperty("Server.IP");
        Client client = new Client();
        client.startClient(host, port);
    }
}
