package or.kr.formulate.korail.util.cms.bloking;

import or.kr.formulate.korail.util.CmsUtil;
import or.kr.formulate.korail.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

        try (Socket socket = new Socket(serverIp, serverPort)) {

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            logger.debug(" test !!");
            // 임시 전문정보를 얻어온다.
            Map<String, Object> data = CmsUtil.test0600();
            // 요청전문을 생성한다.
            String message = CmsUtil.makeMessage("IF0600", data);
            logger.debug("[{}]", message);

            // 소켓에 데이터를 실어서 서버에 요청한다.
            byte[] arrayStream = message.getBytes(encoding);
            oos.writeObject(arrayStream);
            oos.flush();

            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            // 응답 데이터 수신
            byte[] resData = (byte[]) ois.readObject();
            // 응답 데이터 길이(byte)
            int length = resData.length;
            String dataStr = new String(resData, encoding);
            logger.debug("Response Data >  {} [{}]", length, dataStr);

            // 파싱을 위한 객체 생성 : IF0600
            Map<String, String> resMap = CmsUtil.parseMessageCommon(resData, "IF0610");
            logger.debug("resMap {}", resMap);



            oos.flush();
            oos.close();
            ois.close();
            os.close();
            is.close();
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
