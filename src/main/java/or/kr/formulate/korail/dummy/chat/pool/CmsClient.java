package or.kr.formulate.korail.dummy.chat.pool;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class CmsClient {

    private SSLSocket socket;
    private DataOutputStream dos;
    private DataInputStream dis;

    // 연결 설정 메소드
    public void connect(String host, int port) throws Exception {
        // SSL 소켓 팩토리 생성
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) factory.createSocket(host, port);

        // 스트림 초기화
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

        System.out.println("서버에 연결되었습니다.");
    }

    // 전문 송신 메소드
    public void sendMessage(byte[] message) throws IOException {
        dos.writeInt(message.length);
        dos.write(message);
        dos.flush();
        System.out.println("데이터를 전송했습니다.");
    }

    // 응답 수신 메소드
    public byte[] receiveMessage() throws IOException {
        int length = dis.readInt();
        byte[] response = new byte[length];
        dis.readFully(response);
        System.out.println("응답을 수신했습니다.");
        return response;
    }

    // 자원 해제 메소드
    public void close() throws IOException {
        if (dis != null) dis.close();
        if (dos != null) dos.close();
        if (socket != null) socket.close();
        System.out.println("연결이 종료되었습니다.");
    }

    public static void main(String[] args) {
        CmsClient client = new CmsClient();
        try {
            client.connect("localhost", 5000); // 실제 호스트와 포트 사용

            byte[] message = createMessage(); // 전문 생성 로직
            client.sendMessage(message);

            byte[] response = client.receiveMessage();
            // 응답 처리 로직 추가

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] createMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요");

        // 전문 생성 로직 구현
        return (sb.toString()).getBytes(); // 예시로 빈 배열 반환
    }
}
