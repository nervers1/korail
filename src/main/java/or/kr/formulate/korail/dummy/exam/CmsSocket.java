package or.kr.formulate.korail.dummy.exam;

import or.kr.formulate.korail.code.ResponseCode;
import or.kr.formulate.korail.exception.EAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketException;

public class CmsSocket extends Socket {
    private static Logger logger = LoggerFactory.getLogger(CmsSocket.class);
    public CmsSocket() {
        super();
        try {
            logger.debug("CmsSocket constructor: 서버소켓 타임아웃 설정 60초");
            setSoTimeout(60_000);
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
            throw new EAIException("Socket Timeout", ResponseCode.RES_090, e);
        }
    }
    public CmsSocket(String address, int port) {
        super();
        try {
            logger.debug("CmsSocket constructor: 서버소켓 타임아웃 설정 60초");
            setSoTimeout(60_000);
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
            throw new EAIException("Socket Timeout", ResponseCode.RES_090, e);
        }
    }
}
