package or.kr.formulate.korail.util.cms;

import or.kr.formulate.korail.code.ResponseCode;
import or.kr.formulate.korail.exception.EAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class CmsServerSocket extends ServerSocket {
    private static Logger logger = LoggerFactory.getLogger(CmsServerSocket.class);
    public CmsServerSocket() throws IOException {
        super();
        try {
            logger.debug("CmsServerSocket constructor: 서버소켓 타임아웃 설정 60초");
            setSoTimeout(60_000);
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
            throw new EAIException("Socket Timeout", ResponseCode.RES_090, e);
        }
    }
    public CmsServerSocket(String address, int port) throws IOException {
        super();
        try {
            logger.debug("CmsServerSocket constructor: 서버소켓 타임아웃 설정 60초");
            setSoTimeout(60_000);
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
            throw new EAIException("Socket Timeout", ResponseCode.RES_090, e);
        }
    }
}
