package or.kr.formulate.korail.dummy.chat.bloking;

import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");

    public static void main(String[] args) {

        // 전문 요청을 생성한다.
        Scanner input = new Scanner(System.in);

        String serverIp = prop.getProperty("Server.IP");
        int serverPort = Integer.parseInt(prop.getProperty("Server.PORT"));
        logger.debug("Connecting to server. Server IP : {}", serverIp);

        try ( Socket socket = new Socket(serverIp, serverPort)) {
            // 소켓을 생성하여 연결을 요청한다.
            while (true) {
                System.out.print("> ");
                String msg = input.nextLine();
                byte[] arrayStream = msg.getBytes(StandardCharsets.UTF_8);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(arrayStream);
            }


        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void somthing() {
        logger.debug("somthing...");
    }
}
