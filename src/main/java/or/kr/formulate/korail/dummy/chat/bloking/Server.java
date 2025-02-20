package or.kr.formulate.korail.dummy.chat.bloking;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");
    private static final int port = Integer.parseInt(prop.getProperty("Server.PORT"));
    private static final int THREAD_CNT = Integer.parseInt(prop.getProperty("Server.THREAD_CNT"));
    static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_CNT);

    public static void main(String[] args) {
        logger.debug("port: {}", port);
        try {   // 서버소켓을 생성, 7777 포트와 binding
            ServerSocket serverSocket = new ServerSocket(port);
            // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket);
            executor.execute(connectThread);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh : mm : ss ]");
        return f.format(new Date());
    }
}

class ConnectThread extends Thread {
    ServerSocket serverSocket;
    int count = 1;

    ConnectThread(ServerSocket serverSocket) {
        System.out.println(Server.getTime() + " Server opened");
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("    Thread " + count + " is started.");
                ClientThread clientThread = new ClientThread(socket, count);
                Server.executor.execute(clientThread);
                count++;
            }
        } catch (IOException e) {
            System.out.println("    SERVER CLOSE    ");
        }
    }
}


class ClientThread extends Thread {
    Properties prop = PropertyUtil.getInterfaceProp("cms");
    Socket socket;
    int id;

    ClientThread(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    @Override
    public void run() {
        final int bytesLen = Integer.parseInt(prop.getProperty("Server.BYTES"));
        final String encoding = prop.getProperty("Server.ENCODING");
        try {
            while (true) {
                InputStream IS = socket.getInputStream();
                byte[] bt = new byte[bytesLen];
                int size = IS.read(bt);

                String output = new String(bt, 0, size, encoding);
                System.out.println("Thread " + id + " >  " + output);
            }
        } catch (IOException e) {
            System.out.println("    Thread " + id + " is closed. ");
        }
    }
}


