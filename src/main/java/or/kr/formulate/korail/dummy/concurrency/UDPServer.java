package or.kr.formulate.korail.dummy.concurrency;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private static DatagramSocket datagramSocket = null;
    //최대 10개의 스레드를 생성할 수 있는 스레드풀 정의
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("* 서버 종료는 q 입력 *");

        startServer();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String key = scanner.nextLine();

            if (key.equals("q")) {
                break;
            }
        }
        scanner.close();
        stopServer();
    }

    public static void startServer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    datagramSocket = new DatagramSocket(50001);
                    System.out.println(datagramSocket.getLocalPort() + "번 포트에서 서버 실행");

                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
                        datagramSocket.receive(receivePacket);

                        //작업 큐에 작업 삽입
                        executorService.execute(() -> {
                            try {
                                String fromClientData =
                                        new String(
                                                receivePacket.getData(), 0,
                                                receivePacket.getLength(), "UTF-8"
                                        );
                                SocketAddress socketAddress = receivePacket.getSocketAddress();
                                String data = "서버가 보내는 데이터: " + fromClientData;
                                byte[] bytes = data.getBytes("UTF-8");
                                DatagramPacket sendPacket =
                                        new DatagramPacket(bytes, 0, bytes.length, socketAddress);

                                datagramSocket.send(sendPacket);
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

    public static void stopServer() {
        datagramSocket.close();
        executorService.shutdownNow();	//스레드풀 종료
        System.out.println("서버 종료");
    }
}
