package or.kr.formulate.korail.dummy.concurrency;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {
    private static ServerSocket serverSocket = null;
    //최대 10개의 스레드를 생성할 수 있는 스레드풀 정의
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("* 서버 종료는 q 입력 *");

        startTCPServer();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();

            if (input.equals("q")) {
                break;
            }
            System.out.println(input);
        }

        scanner.close();

        stopTCPServer();
    }

    public static void startTCPServer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(50001);
                    System.out.println(serverSocket.getLocalPort() + "번 포트에서 서버 실행");

                    while (true) {
                        System.out.println("서버 연결 요청 대기중");

                        Socket socket = serverSocket.accept();

                        //작업 큐에 작업 삽입
                        executorService.execute(() -> {
                            try {
                                InetSocketAddress inetSocketAddress =
                                        (InetSocketAddress) socket.getRemoteSocketAddress();
                                System.out.println(inetSocketAddress.getHostName()
                                        + "의 연결 요청 수락");

                                DataInputStream dis = new DataInputStream(socket.getInputStream());
                                String data = dis.readUTF();

                                System.out.println(data);

                                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                                dos.writeUTF(data);
                                dos.flush();
                                System.out.println("받은 데이터 클라이언트로 재전송, 데이터: " + data);

                                socket.close();
                                System.out.println(inetSocketAddress.getHostName() + "의 연결 해제");
                            }
                            catch (IOException e) {
                                System.err.println(e);
                            }
                        });
                    }
                }
                catch (IOException e) {
                    System.err.println(e);
                }
            }
        };

        thread.start();
    }

    public static void stopTCPServer() {
        try {
            serverSocket.close();
            executorService.shutdownNow();	//스레드풀 종료
            System.out.println("서버 종료");
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}