package or.kr.formulate.korail.dummy.chat.bloking;

import or.kr.formulate.korail.util.CmsUtil;
import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final Properties prop = PropertyUtil.getInterfaceProp("cms");

    public static void main(String[] args) {
        String serverIp = prop.getProperty("Server.IP");
        final String encoding = prop.getProperty("Server.ENCODING");
        int serverPort = Integer.parseInt(prop.getProperty("Server.PORT"));
        logger.debug("Connecting to server. Server IP : {}", serverIp);


//        Scanner input = new Scanner(System.in);


        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);) {

            // 임시 전문정보를 얻어온다.
            Map<String, Object> data = CmsUtil.test0600();
            // 요청전문을 생성한다.
            String message = CmsUtil.makeMessage("IF0600", data);

            // 소켓에 데이터를 실어서 서버에 요청한다.
            byte[] arrayStream = message.getBytes(encoding);
            objectOutputStream.writeObject(arrayStream);

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
