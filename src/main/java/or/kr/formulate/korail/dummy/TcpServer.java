package or.kr.formulate.korail.dummy;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    public final static int tcpServerPort = 9999;

    public static void main(String[] args) {
        new TcpServer(tcpServerPort);
    }

    public TcpServer(int tcpServerPort) {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(tcpServerPort));
            System.out.println("starting tcp Server: " + tcpServerPort);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected " + socket.getLocalPort() + ", From " + socket.getRemoteSocketAddress().toString());
                Server tcpServer = new Server(socket);  // Thread
                tcpServer.start();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public class Server extends Thread {
        private Socket socket;

        public Server(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                while (true) {
                    // Socket에서 가져온 출력스트림
                    OutputStream os = this.socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);

                    // Socket에서 가져온 입력스트림
                    InputStream is = this.socket.getInputStream();
                    DataInputStream dis = new DataInputStream(is);

                    // read int
                    int recieveLength = dis.readInt();

                    // receive bytes
                    byte receiveByte[] = new byte[recieveLength];
                    dis.readFully(receiveByte, 0, recieveLength);
                    String receiveMessage = new String(receiveByte);
                    System.out.println("receiveMessage : " + receiveMessage);

                    // send bytes
                    String sendMessage = "서버에서 보내는 데이터";
                    byte[] sendBytes = sendMessage.getBytes("UTF-8");
                    int sendLength = sendBytes.length;
                    dos.writeInt(sendLength);
                    dos.write(sendBytes, 0, sendLength);
                    dos.flush();
                }
            } catch (EOFException e) {
                // readInt()를 호출했을 때 더 이상 읽을 내용이 없을 때
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (this.socket != null) {
                        System.out.println("[Socket closed]");
                        System.out.println("Disconnected : " + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort());
                        this.socket.close();
                    }
                } catch (Exception e) {

                }
            }
        }
    }
}