package or.kr.formulate.korail.dummy.chat.pool2;
import java.io.*;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private Socket socket;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public void startClient(String host, int port) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            socket = new Socket(host, port);
            logger.debug("서버에 연결되었습니다.");

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // 데이터 송신
            String message = "클라이언트에서 보내는 메시지입니다.";
            byte[] sendData = message.getBytes("UTF-8");
            sendData(dos, sendData);
            logger.debug("서버에게 메시지를 보냈습니다.");

            // 데이터 수신
            byte[] receivedData = receiveData(dis);
            String response = new String(receivedData, "UTF-8");
            logger.debug("서버로부터 받은 응답: {}", response);

        } catch (IOException e) {
            logger.error("클라이언트 실행 중 오류 발생", e);
        } finally {
            // 스트림 및 소켓 닫기
            closeResources(dis, dos, socket);
        }
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

    // 메인 메소드
    public static void main(String[] args) {
        Client client = new Client();
        client.startClient("localhost", 5000);
    }
}
