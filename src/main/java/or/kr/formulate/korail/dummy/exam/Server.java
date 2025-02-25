package or.kr.formulate.korail.dummy.exam;

import java.io.*;
import java.net.Socket;

public class Server extends Thread {
    private Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        // Socket에서 가져온 출력스트림
        try (OutputStream os = this.socket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);

             // Socket에서 가져온 입력스트림
             InputStream is = this.socket.getInputStream();
             DataInputStream dis = new DataInputStream(is);) {
            while (true) {

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
                //
            }
        }
    }
}
