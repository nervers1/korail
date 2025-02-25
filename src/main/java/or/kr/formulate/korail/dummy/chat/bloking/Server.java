package or.kr.formulate.korail.dummy.chat.bloking;

import or.kr.formulate.korail.util.CmsUtil;
import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
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

        try (InputStream is = socket.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(is);
             OutputStream os = socket.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os);) {


            // 데이터 수신
            byte[] data = (byte[]) ois.readObject();
            // 수신 데이터 길이(byte)
            int length = data.length;

            //
            String dataStr = new String(data, encoding);
            logger.debug("Thread {} length: {}>  {}", id, length, dataStr);

            // 파싱을 위한 객체 생성 : IF0600
            Map<String, String> ifcomm = CmsUtil.parseMessageCommon(data, "IF0600");
            logger.debug("common {}", ifcomm);

            String workDivCd = ifcomm.get("workDivCd");
            String orgCd = ifcomm.get("orgCd");
            String msgKindCd = ifcomm.get("msgKindCd");
            String transactionDivCd = ifcomm.get("transactionDivCd");
            String txFlag = ifcomm.get("txFlag");
            String responseCd = ifcomm.get("responseCd");
            String mngCd = ifcomm.get("mngCd");

            logger.debug("{} 전문수신", msgKindCd);


            switch (msgKindCd) {
                case "0600":
                    logger.debug("[{}/{}] 전문 수신 완료", msgKindCd, mngCd);
                    if ("001".equals(mngCd)) {
                        // 업무개시
                        logger.debug("업무개시");

                        // 업무개시 응답전문 생성

                        // 임시 전문정보를 얻어온다.
                        Map<String, Object> responseMap = CmsUtil.test0610();
                        // 요청전문을 생성한다.
                        String message = CmsUtil.makeMessage("IF0610", responseMap);
                        logger.debug("[{}]", message);

                        // 소켓에 데이터를 실어서 서버에 요청한다.
                        byte[] arrayStream = message.getBytes(encoding);
                        oos.writeObject(arrayStream);



                    } else if ("002".equals(mngCd)) {
                        // 다음 파일 존재
                        logger.debug("다음 파일 존재");
                    } else if ("003".equals(mngCd)) {
                        // 다음 파일 없음
                        logger.debug("다음 파일 없음");
                    } else if ("004".equals(mngCd)) {
                        // 업무종료
                        logger.debug("업무종료");
                    }

                    break;
                case "0630": // 파일정보 수신 요청
                    logger.debug("{} 전문 수신 완료.", msgKindCd);
                    break;
                case "0320": // DATA 송신
                    logger.debug("{} 전문 수신 완료.", msgKindCd);
                    break;
                case "0620": // 결번확인요청
                    logger.debug("{} 전문 수신 완료.", msgKindCd);
                    break;
                case "0310": // 결번 DATA 송신
                    logger.debug("{} 전문 수신 완료.", msgKindCd);
                    break;
                default:
                    break;

            }



//            ois.close();
//            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug("Thread {} is closed. ", id);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}


