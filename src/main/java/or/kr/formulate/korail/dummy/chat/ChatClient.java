package or.kr.formulate.korail.dummy.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    //필드
    Socket socket;
    DataInputStream dis; //수신
    DataOutputStream dos; //송신
    String chatName;

    //메소드: 서버 연결
    public void connect() throws IOException {
        socket = new Socket("localhost", 50001); //연결할 서버의 내용 제공, localhost에 서버 IP를 넣어야함, 50001은 서버의 포트번호
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        System.out.println("[클라이언트] 서버에 연결됨");
    }

    //메소드: JSON 받기
    public void receive() {
        Thread thread = new Thread(() -> { //메시지를 받는 스레드 생성
            try {
                while (true) {
                    String json = dis.readUTF(); //JSON 수신
                    ObjectNode obj = (ObjectNode) new ObjectMapper().readTree(json);

                    String clientIp = obj.get("clientIp").asText();
                    String chatName = obj.get("chatName").asText();
                    String message = obj.get("message").asText();
                    System.out.println("<" + chatName + "@" + clientIp + "> " + message);
                }
            } catch (Exception e1) { //서어봐 연결이 끊겼을 시
                System.out.println("[클라이언트] 서버 연결 끊김");
                System.exit(0);
            }
        });
        thread.start();
    }

    //메소드: JSON 보내기
    public void send(String json) throws IOException {
        dos.writeUTF(json);
        dos.flush();
    }

    //메소드: 서버 연결 종료
    public void unconnect() throws IOException {
        socket.close();
    }

    //메소드: 메인
    public static void main(String[] args) {
        try {
            ChatClient chatClient = new ChatClient();
            chatClient.connect(); //서버에 연결

            Scanner scanner = new Scanner(System.in);
            System.out.println("대화명 입력: ");
            chatClient.chatName = scanner.nextLine();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("command", "incoming"); //command 에 incoming (첫 입장) 내용 전달
            node.put("data", chatClient.chatName); //설정한 채팅네임 전달

            String json = node.toString();
            chatClient.send(json); //전달

            chatClient.receive(); //메시지 수신을 기다림

            System.out.println("--------------------------------------------------");
            System.out.println("보낼 메시지를 입력하고 Enter");
            System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
            System.out.println("--------------------------------------------------");
            while (true) {
                String message = scanner.nextLine();
                if (message.toLowerCase().equals("q")) {
                    break;
                } else {
                    ObjectNode node2 = mapper.createObjectNode();
                    node2.put("command", "message"); //command 종류 : message
                    node2.put("data", message); //data에 보낼 메시지 전달
                    json = node2.toString();
                    chatClient.send(json); //전달
                }
            }
            scanner.close();
            chatClient.unconnect();
        } catch (IOException e) {
            System.out.println("[클라이언트] 서버 연결 안됨");
        }
    }
}
