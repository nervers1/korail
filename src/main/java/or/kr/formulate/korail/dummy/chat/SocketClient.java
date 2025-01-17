package or.kr.formulate.korail.dummy.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {
    //필드
    ChatServer chatServer;
    Socket socket;
    DataInputStream dis; //송신
    DataOutputStream dos; //수신
    String clientIp;
    String chatName;

    //생성자
    public SocketClient(ChatServer chatServer, Socket socket) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress(); //클라이언트의 IP와 포트 정보를 저장
            this.clientIp = isa.getHostString(); //IP정보를 저장
            receive();
        } catch (IOException e) {
        }
    }

    //메소드: JSON 받기
    public void receive() {
        chatServer.threadPool.execute(() -> { //ChatServer의 스레드풀에 작업 전달
            try {
                while (true) {
                    String receiveJson = dis.readUTF();    //JSON을 받아옴

                    JsonNode node = new ObjectMapper().readTree(receiveJson);
                    String command = node.get("command").asText(); //명령 내용에 따라 액션이 달라짐

                    switch (command) {
                        case "incoming": //첫 입장인 경우 incoming 을 받음
                            this.chatName = node.get("data").asText(); //첫 입장일 경우 채팅네임이 data임
                            chatServer.sendToAll(SocketClient.this, "들어오셨습니다.");
                            chatServer.addSocketClient(this);
                            break;
                        case "message":
                            String message = node.get("data").asText(); //첫 입장이 아니면 메시지가 data임
                            chatServer.sendToAll(SocketClient.this, message);
                            break;
                    }
                }
            } catch (IOException e) { //클라이언트와 연결이 끊겼을 경우
                chatServer.sendToAll(SocketClient.this, "나가셨습니다.");
                chatServer.removeSocketClient(SocketClient.this);
            }
        });
    }

    //메소드: JSON 보내기
    public void send(String json) {
        try {
            dos.writeUTF(json);
            dos.flush();
        } catch (IOException e) {
        }
    }

    //메소드: 연결 종료
    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
        }
    }
}