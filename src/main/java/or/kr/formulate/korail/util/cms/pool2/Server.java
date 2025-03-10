package or.kr.formulate.korail.util.cms.pool2;


import or.kr.formulate.korail.util.CmsUtil;
import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");
    final String encoding = prop.getProperty("Server.ENCODING");
    private ServerSocket serverSocket;
    private ExecutorService executor;

    // 서버 시작 메소드
    public void startServer(int port, int threadPoolSize) {
        try {
            serverSocket = new ServerSocket(port);
            logger.debug("서버가 포트 {}에서 시작되었습니다.", port);

            // 스레드 풀 생성
            executor = Executors.newFixedThreadPool(threadPoolSize);

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                logger.debug("클라이언트가 연결되었습니다: {}", clientSocket.getInetAddress());

                // 클라이언트 처리를 위한 작업을 스레드 풀에 제출
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            logger.error("서버 실행 중 오류 발생", e);
        } finally {
            // 서버 종료 시 리소스 해제
            stopServer();
        }
    }

    // 클라이언트 처리 메소드
    private void handleClient(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());


            // 데이터 수신
            byte[] receivedData = receiveData(dis);

            String message = new String(receivedData, encoding);
            logger.debug("클라이언트로부터 받은 메시지: {}", message);


            // 수신 데이터 파싱(수신 데이터를 파싱해서 응답 전문을 생성하고 byte[] 형태로 반환
            byte[] result = parseRequest(receivedData);

            // 응답데이터 생성

            // 데이터 송신
            String response = "서버에서 보내는 응답입니다.";
            byte[] sendData = response.getBytes(encoding);
            sendData(dos, sendData);
            logger.debug("클라이언트에게 응답을 보냈습니다.");

        } catch (IOException e) {
            logger.error("클라이언트 처리 중 오류 발생", e);
        } finally {
            // 스트림 및 소켓 닫기
            closeResources(dis, dos, socket);
        }
    }


    // 전문종별코드
    private String parseMessageKindCode(byte[] receivedData) {
        // 파싱을 위한 객체 생성 : IF0600
        Map<String, String> ifcomm = CmsUtil.parseMessageCommon(receivedData, "IFCOMM");
        return ifcomm.get("msgKindCd");
    }

    // 업무관리정보
    private String parseMngCode(byte[] receivedData) {
        Map<String, String> if0600 = CmsUtil.parseMessageCommon(receivedData, "IF0600");
        return if0600.get("mngCd");
    }

    private byte[] parseRequest(byte[] receivedData) {

        // 2. 수신 데이터를 파싱하여 전문종별코드(msgKindCd), 업무관리정보(mngCd) 추출
        String msgKindCd = parseMessageKindCode(receivedData); // 예: "0600"
        switch (msgKindCd) {
            case "0600":
                String mngCd = parseMngCode(receivedData); // 예: "001"
                if ("001".equals(mngCd)) {
                    // 업무개시

                } else if ("002".equals(mngCd)) {
                    // 파일송수신 완료( 송신할 파일 존재 )
                } else if ("003".equals(mngCd)) {
                    // 파일송수신 완료( 송신할 파일 없음 )
                } else if ("004".equals(mngCd)) {
                    // 업무종료

                }
                break;
            case "0630":
                break;
            default:
        }


        // 입력데이터 파싱로직
        Map<String, String> requestMap = new LinkedHashMap<>();
        // byte[] 형태의 응답전문 생성
        byte[] result = new byte[receivedData.length];
        return result;
    }

    // 데이터 송신 메소드
    private void sendData(DataOutputStream dos, byte[] data) throws IOException {
        dos.writeInt(data.length);
        dos.write(data);
        dos.flush();
    }

    // 데이터 수신 메소드
    private byte[] receiveData(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return data;
    }

    // 자원 해제 메소드
    private void closeResources(DataInputStream dis, DataOutputStream dos, Socket socket) {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
            logger.debug("자원이 해제되었습니다.");
        } catch (IOException e) {
            logger.error("자원 해제 중 오류 발생", e);
        }
    }

    // 서버 중지 메소드
    public void stopServer() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown(); // 새로운 작업 제출을 막음
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow(); // 실행 중인 작업을 중단시킴
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            logger.debug("서버가 종료되었습니다.");
        } catch (IOException e) {
            logger.error("서버 종료 중 오류 발생", e);
        }
    }

    // 메인 메소드
    public static void main(String[] args) {
        int port = 5000;
        int threadPoolSize = 10; // 스레드 풀의 크기 설정

        Server server = new Server();
        server.startServer(port, threadPoolSize);
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        server.stopServer();
    }
}
