package or.kr.formulate.korail.dummy.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer {
    //필드
    ServerSocket serverSocket;
    ExecutorService threadPool = Executors.newFixedThreadPool(100); //스레드풀의 스레드 개수를 100개로 제한
    Map<String, SocketClient> chatRoom = new ConcurrentHashMap<>(); //해시 테이블은 동기화된 메소드를 제공해서 안전
	/*
	 String : chatName@IP
	 SocketClient : SocketClient 객체
	 */

    //메소드: 서버 시작
    public void start() throws IOException {
        serverSocket = new ServerSocket(50001);    //50001번 포트로 바인딩
        System.out.println("[서버] 시작됨");

        Thread thread = new Thread(() -> { //서버에 연결 요청 수락을 대기하는 스레드
            try {
                while (true) {
                    Socket socket = serverSocket.accept(); //연결 요청이 들어오면 수락
                    SocketClient sc = new SocketClient(ChatServer.this, socket); // SocketClient 생성자에 이 클래스 객체와 socket 객체를 매개값으로 넘김
                }
            } catch (IOException e) {
            }
        });
        thread.start(); //스레드 시작
    }

    //메소드: 클라이언트 연결시 SocketClient 생성 및 추가
    public void addSocketClient(SocketClient socketClient) {
        String key = socketClient.chatName + "@" + socketClient.clientIp;
        chatRoom.put(key, socketClient); //chatRoom에 <chatName@IP, socketClient> 추가
        System.out.println("입장: " + key);
        System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n"); //현재 서버에 몇명이 들어왔는지 표시
    }

    //메소드: 클라이언트 연결 종료시 SocketClient 제거
    public void removeSocketClient(SocketClient socketClient) {
        String key = socketClient.chatName + "@" + socketClient.clientIp;
        chatRoom.remove(key); //chatRoom에서 해당 키 제거
        System.out.println("나감: " + key);
        System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
    }

    //메소드: 모든 클라이언트에게 메시지 보냄
    public void sendToAll(SocketClient sender, String message) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        root.put("clientIp", sender.clientIp); //JSON에 IP 추가
        root.put("chatName", sender.chatName); //JSON에 chatName 추가
        root.put("message", message);  //JSON에 보낼 메시지 추가
        String json = root.toString(); //String 타입으로 변환


        Collection<SocketClient> socketClients = chatRoom.values(); //chatRoom의 키가 아닌 값들만 뽑아옴
        for (SocketClient sc : socketClients) { //연결된 클라이언트들에게
            if (sc == sender) continue; //발송자를 제외하고
            sc.send(json); //메시지 전송
        }
    }

    //메소드: 서버 종료
    public void stop() {
        try {
            serverSocket.close();
            threadPool.shutdownNow();
            chatRoom.values().stream().forEach(sc -> sc.close()); //내부 반복자를 이용하여 연결된 클라이언트 모두 종료
            System.out.println("[서버] 종료됨 ");
        } catch (IOException e1) {
        }
    }
    // Graceful shutdown
    private void shutdownAndAwaitTermination(ExecutorService executorService) {
        // 새로운 스레드가 실행되지 않도록 ExecutorService 를 종료시킵니다.
        executorService.shutdown();
        try {
            // 긴 시간 동안 ExecutorService 가 종료되기를 기다립니다.
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
                // 시간 초과 후에도 ExecutorService 가 종료되지 않으면 강제로 종료합니다.
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 대기 중 현재 스레드가 인터럽트되면 강제로 종료합니다.
            executorService.shutdownNow();
            // 현재 스레드를 다시 인터럽트하여 인터럽트 상태를 복원합니다.
            Thread.currentThread().interrupt();
        }
    }

    //메소드: 메인
    public static void main(String[] args) {
        try {
            ChatServer chatServer = new ChatServer();
            chatServer.start();

            System.out.println("----------------------------------------------------");
            System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
            System.out.println("----------------------------------------------------");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String key = scanner.nextLine();
                if (key.equals("q")) break;
            }
            scanner.close();
            chatServer.stop();
        } catch (IOException e) {
            System.out.println("[서버] " + e.getMessage());
        }
    }
}
