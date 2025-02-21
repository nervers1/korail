package or.kr.formulate.korail.dummy.chat.bloking;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

}

class ConnectThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConnectThread.class);
    ServerSocket serverSocket;
    int count = 1;

    ConnectThread(ServerSocket serverSocket) {
        logger.debug("{} Server opened", getTime());
//        System.out.println(getTime() + " Server opened");
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("    Thread {} is started.", count);
                ClientThread clientThread = new ClientThread(socket, count);
                Server.executor.execute(clientThread);
                count++;
            }
        } catch (IOException e) {
            logger.debug("    SERVER CLOSE    ");
        }
    }

    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh : mm : ss ]");
        return f.format(new Date());
    }
}


class ClientThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClientThread.class);

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

/*
        try (InputStream is = socket.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(is)) {
            while (true) {
                byte[] dataBytes = (byte[])ois.readObject();
                int length = dataBytes.length;

                String data = new String(dataBytes, encoding);
                logger.debug("Thread {} length: {}>  {}", id, length, data);
                *//*
                InputStream IS = socket.getInputStream();
                byte[] bt = new byte[bytesLen];
                int size = IS.read(bt);

                String output = new String(bt, 0, size, encoding);
                logger.debug("Thread {} >  {}", id, output);*//*
            }*/
        try (InputStream is = socket.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(is);
             OutputStream os = socket.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os)) {

            while (true) {
                byte[] data = (byte[])ois.readObject();
                int length = data.length;

                String dataStr = new String(data, encoding);
                logger.debug("Thread {} length: {}>  {}", id, length, dataStr);

                // 읽은 데이터 파싱
                final byte[] buffer = new byte[4];
                System.arraycopy(data, 0, buffer, 0, buffer.length);
                logger.debug("Thread {} buffer: {}", id, new String(buffer));


                InputStream IS = socket.getInputStream();
                byte[] bt = new byte[bytesLen];
                int size = IS.read(bt);

                String output = new String(bt, 0, size, encoding);
                logger.debug("Thread {} >  {}", id, output);
            }
        } catch (IOException e) {
            logger.debug("Thread {} is closed. ", id);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}


