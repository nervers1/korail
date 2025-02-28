package or.kr.formulate.korail.util.cms.nonblock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;

    private final ExecutorService executor;

    public Server() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port: {}", PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());

                // 클라이언트 요청을 비동기로 처리
                CompletableFuture.runAsync(() -> handleClient(clientSocket), executor)
                        .exceptionally(ex -> {
                            logger.error("클라이언트 처리 중 예외 발생: ", ex);
                            return null;
                        });

            }
        } catch (IOException e) {
            logger.error("Server error: ", e);
        } finally {
            executor.shutdown();
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            logger.debug("Preparing to handle client: {}", socket.getRemoteSocketAddress());

            // 1. 소켓 연결상태 확인
            if (!socket.isConnected()) {
                logger.error("Socket is not connected. Exiting client handler.");
                return;
            }

            // 2. 데이터 수신
            byte[] request = readRequestData(input);
            if (request == null) return;
            logger.info("Received data from client: {}", new String(request));

            // 3. 클라이언트 작업 처리
            String parsedRequest = new String(request);
            String responseMessage = "Processed: " + parsedRequest;
            byte[] response = responseMessage.getBytes();

            // 4. 응답 데이터 전송
            sendResponseData(output, response);

            logger.info("Response sent to client: {}", responseMessage);

        } catch (IOException e) {
            logger.error("Error handling client: ", e);
        } finally {
            try {
                socket.close();
                logger.info("Client disconnected.");
            } catch (IOException e) {
                logger.error("Error closing client socket: ", e);
            }
        }
    }

    private static void sendResponseData(DataOutputStream output, byte[] response) throws IOException {
        synchronized (output) {  // 스트림 동기화를 추가
            output.writeInt(response.length);
            output.write(response);
            output.flush();
        }
    }

    private static byte[] readRequestData(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            logger.error("Invalid data length received: {}", length);
            return null;
        }

        byte[] request = new byte[length];
        input.readFully(request);
        return request;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}