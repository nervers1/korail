package or.kr.formulate.korail.dummy.chat.pool;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.*;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");
    private final String encoding = prop.getProperty("Server.ENCODING");

    // 서버 시작 메소드
    public void startServer(int port, int threadCount) {
        try {
            serverSocket = new ServerSocket(port);
            logger.debug("서버가 포트 {}에서 시작되었습니다.", port);

            // 스레드 풀 생성
            executor = Executors.newFixedThreadPool(threadCount);

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                logger.debug("클라이언트가 연결되었습니다. [{}]", clientSocket.getInetAddress());

                // 클라이언트 처리를 위한 작업을 스레드 풀에 제출
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 서버 종료 시 리소스 해제
            stopServer();
        }
    }

    // 서버 중지 메소드
    public void stopServer() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            logger.debug("서버가 종료되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트 처리를 위한 내부 클래스
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // 스트림 초기화
                initializeStreams();

                // 데이터 수신 및 송신
                receiveData();
                sendData();

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
            outputStream = clientSocket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            inputStream = clientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
        }

        // 데이터 수신 메소드
        private void receiveData() throws IOException, ClassNotFoundException {
            byte[] receivedBytes = (byte[]) objectInputStream.readObject();
            String receivedMessage = new String(receivedBytes, encoding);
            logger.debug("클라이언트로부터 받은 메시지: [{}]", receivedMessage);
        }

        // 데이터 송신 메소드
        private void sendData() throws IOException {
            String message = "서버에서 보내는 메시지입니다.";
            byte[] sendBytes = message.getBytes(encoding);
            objectOutputStream.writeObject(sendBytes);
            objectOutputStream.flush();
            logger.debug("클라이언트에게 메시지를 보냈습니다.");
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
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                logger.debug("클라이언트 소켓이 닫혔습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 메인 메소드
    public static void main(String[] args) {
        final int port = Integer.parseInt(prop.getProperty("Server.PORT"));
        final int THREAD_CNT = Integer.parseInt(prop.getProperty("Server.THREAD_CNT"));
        Server server = new Server();
        server.startServer(port, THREAD_CNT);
    }
}
